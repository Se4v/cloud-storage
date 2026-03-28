package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.minio.CreateMultipartUploadResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.config.MinioConfig;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.args.InitUploadArgs;
import org.example.backend.model.args.MergeChunksArgs;
import org.example.backend.model.args.SimpleUploadArgs;
import org.example.backend.model.args.UploadChunkArgs;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.view.InitUploadView;
import org.example.backend.model.view.MergeChunksView;
import org.example.backend.model.view.SimpleUploadView;
import org.example.backend.model.view.UploadChunkView;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UploadService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private MinioConfig minioConfig;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MinioAsyncClient minioAsyncClient;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DriveMapper driveMapper;
    @Lazy
    @Autowired
    private UploadService self;

    private static final String TASK_KEY_PREFIX = "upload:task:";
    private static final String CHUNKS_KEY_PREFIX = "upload:chunks:";
    private static final String LOCK_KEY_PREFIX = "upload:lock:";

    private static final String FIELD_UPLOAD_TYPE = "uploadType";
    private static final String FIELD_UPLOAD_ID = "uploadId";
    private static final String FIELD_ENTRY_NAME = "entryName";
    private static final String FIELD_FILE_SIZE = "fileSize";
    private static final String FIELD_MIME_TYPE = "mimeType";
    private static final String FIELD_UPLOADED_CHUNKS = "uploadedChunks";
    private static final String FIELD_TOTAL_CHUNKS = "totalChunks";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_DRIVE_ID = "driveId";
    private static final String FIELD_PARENT_ID = "parentId";
    private static final String FIELD_BUCKET_NAME = "bucketName";
    private static final String FIELD_OBJECT_NAME = "objectName";
    private static final String FIELD_CREATE_TIME = "createTime";

    private static final String UPLOAD_TYPE_DIRECT = "direct";
    private static final String UPLOAD_TYPE_MULTIPART = "multipart";

    private static final long UPLOAD_THRESHOLD = 10 * 1024 * 1024L;
    private static final long TASK_EXPIRE_HOURS = 24;
    private static final long LOCK_WAIT_SECONDS = 10;
    private static final long LOCK_LEASE_SECONDS = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public InitUploadView initUpload(InitUploadArgs args, Long userId) {
        List<InitUploadView.View> viewList = new ArrayList<>();
        int successCount = 0;

        if (args.getArgList() == null || args.getArgList().isEmpty()) {
            return InitUploadView.builder()
                    .viewList(viewList)
                    .totalCount(0)
                    .successCount(0)
                    .build();
        }

        for (InitUploadArgs.Arg arg : args.getArgList()) {
            try {
                InitUploadView.View view = initSingleUpload(arg, args, userId);
                if (Boolean.TRUE.equals(view.getSuccess())) {
                    successCount++;
                }
                viewList.add(view);
            } catch (Exception e) {
                log.error("初始化上传失败: entryName={}, sha256={}",
                        arg == null ? null : arg.getEntryName(),
                        arg == null ? null : arg.getSha256(),
                        e);
                viewList.add(InitUploadView.View.builder()
                        .entryName(arg == null ? null : arg.getEntryName())
                        .success(false)
                        .message("初始化失败: " + e.getMessage())
                        .build());
            }
        }

        return InitUploadView.builder()
                .viewList(viewList)
                .totalCount(args.getArgList().size())
                .successCount(successCount)
                .build();
    }

    public SimpleUploadView simpleUpload(SimpleUploadArgs args, Long userId) {
        String taskKey = getTaskKey(userId, args.getSha256());
        Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
        String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
        if (taskMap.isEmpty() || !UPLOAD_TYPE_DIRECT.equals(uploadType)) {
            throw new BusinessException("直传任务不存在或已过期");
        }

        String bucketName = taskMap.get(FIELD_BUCKET_NAME).toString();
        String objectName = taskMap.get(FIELD_OBJECT_NAME).toString();
        ensureObjectExists(bucketName, objectName);

        self.persistUploadedFile(taskMap, args.getSha256(), userId);
        redisTemplate.delete(taskKey);

        String entryName = taskMap.get(FIELD_ENTRY_NAME).toString();
        return SimpleUploadView.builder()
                .entryName(entryName)
                .sha256(args.getSha256())
                .success(true)
                .build();
    }

    public UploadChunkView uploadChunk(UploadChunkArgs args, Long userId) {
        List<UploadChunkView.View> viewList = new ArrayList<>();
        for (UploadChunkArgs.Arg arg : args.getArgList()) {
            try {
                String taskKey = getTaskKey(userId, arg.getSha256());
                Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
                if (taskMap.isEmpty()) {
                    throw new BusinessException("任务不存在或已过期");
                }

                String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
                if (!UPLOAD_TYPE_MULTIPART.equals(uploadType)) {
                    throw new BusinessException("当前任务不是分片上传任务");
                }

                if (arg.getEtag() == null || arg.getEtag().isBlank()) throw new BusinessException("");

                String chunksKey = getChunksKey(userId, arg.getSha256());
                Object existingEtag = redisTemplate.opsForHash().get(chunksKey, arg.getChunkNumber());
                if (existingEtag == null) {
                    redisTemplate.opsForHash().put(chunksKey, arg.getChunkNumber(), arg.getEtag());
                    redisTemplate.opsForHash().increment(taskKey, FIELD_UPLOADED_CHUNKS, 1);
                } else if (!Objects.equals(String.valueOf(existingEtag), arg.getEtag())) {
                    redisTemplate.opsForHash().put(chunksKey, arg.getChunkNumber(), arg.getEtag());
                }

                redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
                redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

                String uploadId = taskMap.get(FIELD_UPLOAD_ID).toString();
                viewList.add(UploadChunkView.View.builder()
                        .uploadId(uploadId)
                        .chunkNumber(arg.getChunkNumber())
                        .success(true)
                        .build());
            } catch (Exception e) {
                log.error("记录分片失败: sha256={}, chunk={}",
                        arg == null ? null : arg.getSha256(),
                        arg == null ? null : arg.getChunkNumber(),
                        e);
                viewList.add(UploadChunkView.View.builder()
                        .chunkNumber(arg == null ? null : arg.getChunkNumber())
                        .success(false)
                        .build());
            }
        }

        return UploadChunkView.builder().viewList(viewList).build();
    }

    public MergeChunksView mergeChunks(MergeChunksArgs args, Long userId) {
        String taskKey = getTaskKey(userId, args.getSha256());
        String chunksKey = getChunksKey(userId, args.getSha256());

        try {
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
            if (taskMap.isEmpty() || !UPLOAD_TYPE_MULTIPART.equals(uploadType)) {
                throw new BusinessException("分片上传任务不存在或已过期");
            }

            int totalChunks = Integer.parseInt(taskMap.get(FIELD_TOTAL_CHUNKS).toString());
            Map<Object, Object> chunksMap = redisTemplate.opsForHash().entries(chunksKey);
            if (chunksMap.size() < totalChunks) {
                throw new BusinessException("分片未上传完整，无法合并");
            }

            String bucketName = taskMap.get(FIELD_BUCKET_NAME).toString();
            String objectName = taskMap.get(FIELD_OBJECT_NAME).toString();
            String uploadId = taskMap.get(FIELD_UPLOAD_ID).toString();
            List<Part> parts = chunksMap.entrySet().stream()
                    .map(e -> new Part(Integer.parseInt(String.valueOf(e.getKey())), String.valueOf(e.getValue())))
                    .sorted(Comparator.comparingInt(Part::partNumber))
                    .toList();

            minioAsyncClient.completeMultipartUploadAsync(
                    bucketName,
                    null,
                    objectName,
                    uploadId,
                    parts.toArray(new Part[0]),
                    null,
                    null
            ).get();

            ensureObjectExists(bucketName, objectName);
            self.persistUploadedFile(taskMap, args.getSha256(), userId);
            redisTemplate.delete(taskKey);
            redisTemplate.delete(chunksKey);

            return MergeChunksView.builder()
                    .sha256(args.getSha256())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("合并分片失败: sha256={}", args.getSha256(), e);
            return MergeChunksView.builder()
                    .sha256(args.getSha256())
                    .success(false)
                    .build();
        }
    }

    private InitUploadView.View initSingleUpload(InitUploadArgs.Arg arg, InitUploadArgs args, Long userId) throws Exception {
        String lockKey = getLockKey(userId, arg.getSha256());
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            LambdaQueryWrapper<Storage> storageQuery = new LambdaQueryWrapper<>();
            storageQuery.eq(Storage::getSha256, arg.getSha256());
            Storage existedStorage = storageMapper.selectOne(storageQuery);
            if (existedStorage != null && Objects.equals(existedStorage.getEnabled(), 1)) {
                self.createEntryForInstantUpload(existedStorage, arg, args, userId);
                return InitUploadView.View.builder()
                        .entryName(arg.getEntryName())
                        .success(true)
                        .message("秒传成功")
                        .isSkip(true)
                        .isMultipart(false)
                        .sha256(arg.getSha256())
                        .build();
            }

            String taskKey = getTaskKey(userId, arg.getSha256());
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            if (!taskMap.isEmpty()) {
                return buildResumeView(taskMap, userId, arg);
            }

            String objectName = generateObjectName(arg.getSha256(), arg.getEntryName());
            if (arg.getFileSize() <= UPLOAD_THRESHOLD) {
                saveTaskToRedis(taskKey, UPLOAD_TYPE_DIRECT, null, objectName, arg, args, userId);
                String uploadUrl = generateSinglePresignedUrl(minioConfig.getBucketName(), objectName);
                return InitUploadView.View.builder()
                        .entryName(arg.getEntryName())
                        .success(true)
                        .message("获取小文件直传链接成功")
                        .isSkip(false)
                        .isMultipart(false)
                        .sha256(arg.getSha256())
                        .uploadUrl(uploadUrl)
                        .build();
            }

            CompletableFuture<CreateMultipartUploadResponse> future = minioAsyncClient.createMultipartUploadAsync(
                    minioConfig.getBucketName(),
                    null,
                    objectName,
                    null,
                    null
            );
            String uploadId = future.get().result().uploadId();

            saveTaskToRedis(taskKey, UPLOAD_TYPE_MULTIPART, uploadId, objectName, arg, args, userId);
            List<String> chunkUrls = generateChunkUrls(uploadId, objectName, arg.getTotalChunks());

            // 初始化分片记录表
            // String chunksKey = getChunksKey(args.getUserId(), arg.getSha256());
            // redisTemplate.opsForHash().putAll(chunksKey, new HashMap<>());
            // redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

            return InitUploadView.View.builder()
                    .entryName(arg.getEntryName())
                    .success(true)
                    .message("大文件分片上传初始化成功")
                    .isSkip(false)
                    .isMultipart(true)
                    .sha256(arg.getSha256())
                    .uploadedChunks(List.of())
                    .chunkUrls(chunkUrls)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("初始化上传被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private InitUploadView.View buildResumeView(Map<Object, Object> taskMap, Long userId, InitUploadArgs.Arg arg) throws Exception {
        String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
        String objectName = taskMap.get(FIELD_OBJECT_NAME).toString();
        String bucketName = taskMap.get(FIELD_BUCKET_NAME).toString();

        if (UPLOAD_TYPE_DIRECT.equals(uploadType)) {
            String uploadUrl = generateSinglePresignedUrl(bucketName, objectName);
            return InitUploadView.View.builder()
                    .entryName(arg.getEntryName())
                    .success(true)
                    .message("小文件断点续传")
                    .isSkip(false)
                    .isMultipart(false)
                    .sha256(arg.getSha256())
                    .uploadUrl(uploadUrl)
                    .build();
        }

        if (!UPLOAD_TYPE_MULTIPART.equals(uploadType)) {
            throw new BusinessException("未知上传类型: " + uploadType);
        }

        String uploadId = taskMap.get(FIELD_UPLOAD_ID).toString();
        int totalChunks = Integer.parseInt(taskMap.get(FIELD_TOTAL_CHUNKS).toString());
        List<Integer> uploadedChunks = getUploadedChunkNumbers(userId, arg.getSha256());
        List<String> chunkUrls = regenerateChunkUrls(uploadId, objectName, totalChunks, uploadedChunks);

        return InitUploadView.View.builder()
                .entryName(arg.getEntryName())
                .success(true)
                .message("大文件分片断点续传")
                .isSkip(false)
                .isMultipart(true)
                .sha256(arg.getSha256())
                .uploadedChunks(uploadedChunks)
                .chunkUrls(chunkUrls)
                .build();
    }

    @Transactional
    public void createEntryForInstantUpload(Storage storage, InitUploadArgs.Arg arg, InitUploadArgs args, Long userId) {
        Entry entry = Entry.builder()
                .driveId(args.getDriveId())
                .parentId(args.getParentId())
                .userId(userId)
                .storageId(storage.getId())
                .entryName(arg.getEntryName())
                .entryType(1)
                .fileSize(arg.getFileSize())
                .fileExt(getFileSuffix(arg.getEntryName()))
                .status(1)
                .build();

        int entryCount = entryMapper.insert(entry);
        if (entryCount != 1) {
            throw new BusinessException("秒传写入文件条目失败");
        }

        LambdaUpdateWrapper<Storage> storageUpdate = new LambdaUpdateWrapper<>();
        storageUpdate.setIncrBy(Storage::getRefCount, 1).eq(Storage::getId, storage.getId());
        int refCount = storageMapper.update(storageUpdate);
        if (refCount != 1) {
            throw new BusinessException("秒传增加引用计数失败");
        }

        LambdaUpdateWrapper<Drive> driveUpdate = new LambdaUpdateWrapper<>();
        driveUpdate.setIncrBy(Drive::getUsedQuota, arg.getFileSize()).eq(Drive::getId, args.getDriveId());
        int driveCount = driveMapper.update(driveUpdate);
        if (driveCount != 1) {
            throw new BusinessException("秒传更新空间配额失败");
        }
    }

    @Transactional
    public void persistUploadedFile(Map<Object, Object> taskMap, String sha256, Long userId) {
        String entryName = taskMap.get(FIELD_ENTRY_NAME).toString();
        Long fileSize = Long.parseLong(taskMap.get(FIELD_FILE_SIZE).toString());
        String mimeType = taskMap.get(FIELD_MIME_TYPE).toString();
        Long driveId = Long.parseLong(taskMap.get(FIELD_DRIVE_ID).toString());
        Long parentId = Long.parseLong(taskMap.get(FIELD_PARENT_ID).toString());
        String bucketName = taskMap.get(FIELD_BUCKET_NAME).toString();
        String objectName = taskMap.get(FIELD_OBJECT_NAME).toString();
        String fileExt = getFileSuffix(entryName);

        LambdaQueryWrapper<Storage> storageQuery = new LambdaQueryWrapper<>();
        storageQuery.eq(Storage::getSha256, sha256);
        Storage storage = storageMapper.selectOne(storageQuery);
        Long storageId;
        if (storage != null && Objects.equals(storage.getEnabled(), 1)) {
            LambdaUpdateWrapper<Storage> storageUpdate = new LambdaUpdateWrapper<>();
            storageUpdate.setIncrBy(Storage::getRefCount, 1).eq(Storage::getId, storage.getId());
            int refCount = storageMapper.update(storageUpdate);
            if (refCount != 1) {
                throw new BusinessException("增加引用计数失败");
            }
            storageId = storage.getId();
        } else {
            Storage newStorage = Storage.builder()
                    .originalName(entryName)
                    .fileExt(fileExt)
                    .fileSize(fileSize)
                    .sha256(sha256)
                    .bucketName(bucketName)
                    .objectKey(objectName)
                    .mimeType(mimeType)
                    .enabled(1)
                    .refCount(1)
                    .creatorId(userId)
                    .build();
            int storageCount = storageMapper.insert(newStorage);
            if (storageCount != 1 || newStorage.getId() == null) {
                throw new BusinessException("存储记录写入失败");
            }
            storageId = newStorage.getId();
        }

        Entry entry = Entry.builder()
                .driveId(driveId)
                .userId(userId)
                .parentId(parentId)
                .storageId(storageId)
                .entryName(entryName)
                .entryType(1)
                .fileSize(fileSize)
                .fileExt(fileExt)
                .status(1)
                .build();
        int entryCount = entryMapper.insert(entry);
        if (entryCount != 1) {
            throw new BusinessException("文件条目写入失败");
        }

        LambdaUpdateWrapper<Drive> driveUpdate = new LambdaUpdateWrapper<>();
        driveUpdate.setIncrBy(Drive::getUsedQuota, fileSize).eq(Drive::getId, driveId);
        int driveCount = driveMapper.update(driveUpdate);
        if (driveCount != 1) {
            throw new BusinessException("空间配额更新失败");
        }
    }

    private void saveTaskToRedis(String taskKey, String uploadType, String uploadId,
                                 String objectName, InitUploadArgs.Arg arg, InitUploadArgs args, Long userId) {
        int totalChunks = (arg.getTotalChunks() == null || arg.getTotalChunks() <= 0) ? 1 : arg.getTotalChunks();
        Map<String, String> task = new HashMap<>();
        task.put(FIELD_UPLOAD_TYPE, uploadType);
        if (uploadId != null) {
            task.put(FIELD_UPLOAD_ID, uploadId);
        }
        task.put(FIELD_ENTRY_NAME, arg.getEntryName());
        task.put(FIELD_FILE_SIZE, String.valueOf(arg.getFileSize()));
        task.put(FIELD_MIME_TYPE, arg.getMimeType() == null ? "application/octet-stream" : arg.getMimeType());
        task.put(FIELD_UPLOADED_CHUNKS, "0");
        task.put(FIELD_TOTAL_CHUNKS, String.valueOf(totalChunks));
        task.put(FIELD_USER_ID, String.valueOf(userId));
        task.put(FIELD_DRIVE_ID, String.valueOf(args.getDriveId()));
        task.put(FIELD_PARENT_ID, String.valueOf(args.getParentId()));
        task.put(FIELD_BUCKET_NAME, minioConfig.getBucketName());
        task.put(FIELD_OBJECT_NAME, objectName);
        task.put(FIELD_CREATE_TIME, LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(taskKey, task);
        redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    private String generateSinglePresignedUrl(String bucketName, String objectName) throws Exception {
        return minioAsyncClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(1, TimeUnit.HOURS)
                        .build()
        );
    }

    private List<String> generateChunkUrls(String uploadId, String objectName, int totalChunks) throws Exception {
        List<String> urls = new ArrayList<>(totalChunks);
        for (int i = 1; i <= totalChunks; i++) {
            urls.add(minioAsyncClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .extraQueryParams(Map.of("uploadId", uploadId, "partNumber", String.valueOf(i)))
                            .build()
            ));
        }
        return urls;
    }

    private List<String> regenerateChunkUrls(String uploadId, String objectName,
                                             int totalChunks, List<Integer> uploadedChunks) throws Exception {
        Set<Integer> uploadedSet = new HashSet<>(uploadedChunks);
        List<String> urls = new ArrayList<>(totalChunks);
        for (int i = 1; i <= totalChunks; i++) {
            if (uploadedSet.contains(i)) {
                urls.add(null);
                continue;
            }
            urls.add(minioAsyncClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .extraQueryParams(Map.of("uploadId", uploadId, "partNumber", String.valueOf(i)))
                            .build()
            ));
        }
        return urls;
    }

    private List<Integer> getUploadedChunkNumbers(Long userId, String sha256) {
        String chunksKey = getChunksKey(userId, sha256);
        Map<Object, Object> chunks = redisTemplate.opsForHash().entries(chunksKey);
        return chunks.keySet().stream()
                .map(k -> Integer.parseInt(k.toString()))
                .sorted()
                .toList();
    }

    private void ensureObjectExists(String bucketName, String objectName) {
        try {
            minioAsyncClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            ).get();
        } catch (Exception e) {
            throw new BusinessException("对象不存在或不可访问");
        }
    }

    private String getTaskKey(Long userId, String sha256) {
        return TASK_KEY_PREFIX + userId + ":" + sha256;
    }

    private String getChunksKey(Long userId, String sha256) {
        return CHUNKS_KEY_PREFIX + userId + ":" + sha256;
    }

    private String getLockKey(Long userId, String sha256) {
        return LOCK_KEY_PREFIX + userId + ":" + sha256;
    }

    private String generateObjectName(String sha256, String entryName) {
        if (sha256 == null || sha256.length() < 4) {
            throw new BusinessException("SHA256格式错误");
        }
        String safeName = entryName == null ? "file" : entryName.replace("/", "_").replace("\\", "_");
        return LocalDateTime.now().format(DATE_FORMATTER)
                + "/"
                + sha256.substring(0, 4)
                + "/"
                + System.currentTimeMillis()
                + "_"
                + safeName;
    }

    private String getFileSuffix(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
