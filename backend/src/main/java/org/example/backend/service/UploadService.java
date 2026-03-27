package org.example.backend.service;

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
import org.example.backend.model.args.SimpleUploadArgs;
import org.example.backend.model.args.UploadChunkArgs;
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

    private static final long TASK_EXPIRE_HOURS = 24;
    private static final long LOCK_WAIT_SECONDS = 10;
    private static final long LOCK_LEASE_SECONDS = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public InitUploadView initUpload(InitUploadArgs args, Long userId) {
        validateInitRequest(args, userId);

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
                InitUploadView.View view = initSingleUpload(arg, args);
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
        if (args == null || args.getSha256() == null || args.getSha256().isBlank()) {
            throw new BusinessException("sha256不能为空");
        }
        if (args.getUserId() != null && !Objects.equals(args.getUserId(), userId)) {
            throw new BusinessException("非法的用户ID");
        }

        String taskKey = getTaskKey(userId, args.getSha256());
        Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
        if (taskMap.isEmpty() || !UPLOAD_TYPE_DIRECT.equals(getString(taskMap, FIELD_UPLOAD_TYPE))) {
            throw new BusinessException("直传任务不存在或已过期");
        }
        validateTaskOwner(taskMap, userId);

        String bucketName = getString(taskMap, FIELD_BUCKET_NAME);
        String objectName = getString(taskMap, FIELD_OBJECT_NAME);
        ensureObjectExists(bucketName, objectName);

        self.persistUploadedFile(taskMap, args.getSha256(), userId);
        cleanUploadCache(taskKey, null);

        return SimpleUploadView.builder()
                .entryName(getString(taskMap, FIELD_ENTRY_NAME))
                .sha256(args.getSha256())
                .success(true)
                .build();
    }

    public UploadChunkView uploadChunk(UploadChunkArgs args, Long userId) {
        if (args == null || args.getArgList() == null || args.getArgList().isEmpty()) {
            return UploadChunkView.builder().viewList(List.of()).build();
        }

        List<UploadChunkView.View> viewList = new ArrayList<>();
        for (UploadChunkArgs.Arg arg : args.getArgList()) {
            try {
                if (arg == null || arg.getSha256() == null || arg.getSha256().isBlank()) {
                    throw new BusinessException("sha256不能为空");
                }
                if (arg.getChunkNumber() == null || arg.getChunkNumber().isBlank()) {
                    throw new BusinessException("chunkNumber不能为空");
                }
                if (arg.getEtag() == null || arg.getEtag().isBlank()) {
                    throw new BusinessException("etag不能为空");
                }

                String taskKey = getTaskKey(userId, arg.getSha256());
                Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
                if (taskMap.isEmpty()) {
                    throw new BusinessException("任务不存在或已过期");
                }
                if (!UPLOAD_TYPE_MULTIPART.equals(getString(taskMap, FIELD_UPLOAD_TYPE))) {
                    throw new BusinessException("当前任务不是分片上传任务");
                }
                validateTaskOwner(taskMap, userId);

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

                viewList.add(UploadChunkView.View.builder()
                        .uploadId(getString(taskMap, FIELD_UPLOAD_ID))
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

    public MergeChunksView mergeChunks(String sha256, Long userId) {
        if (sha256 == null || sha256.isBlank()) {
            throw new BusinessException("sha256不能为空");
        }

        String taskKey = getTaskKey(userId, sha256);
        String chunksKey = getChunksKey(userId, sha256);

        try {
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            if (taskMap.isEmpty() || !UPLOAD_TYPE_MULTIPART.equals(getString(taskMap, FIELD_UPLOAD_TYPE))) {
                throw new BusinessException("分片上传任务不存在或已过期");
            }
            validateTaskOwner(taskMap, userId);

            int totalChunks = getInteger(taskMap, FIELD_TOTAL_CHUNKS);
            Map<Object, Object> chunksMap = redisTemplate.opsForHash().entries(chunksKey);
            List<Integer> missing = getMissingChunkNumbers(totalChunks, chunksMap);
            if (!missing.isEmpty()) {
                throw new BusinessException("分片未上传完整，缺失分片: " + missing);
            }

            String bucketName = getString(taskMap, FIELD_BUCKET_NAME);
            String objectName = getString(taskMap, FIELD_OBJECT_NAME);
            String uploadId = getString(taskMap, FIELD_UPLOAD_ID);
            List<Part> parts = buildSortedParts(chunksMap);

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
            self.persistUploadedFile(taskMap, sha256, userId);
            cleanUploadCache(taskKey, chunksKey);

            return MergeChunksView.builder()
                    .sha256(sha256)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("合并分片失败: sha256={}", sha256, e);
            return MergeChunksView.builder()
                    .sha256(sha256)
                    .success(false)
                    .build();
        }
    }

    private InitUploadView.View initSingleUpload(InitUploadArgs.Arg arg, InitUploadArgs args) throws Exception {
        validateSingleArg(arg);

        String lockKey = getLockKey(args.getUserId(), arg.getSha256());
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            Storage existedStorage = storageMapper.selectBySha256(arg.getSha256());
            if (existedStorage != null && Objects.equals(existedStorage.getEnabled(), 1)) {
                self.createEntryForInstantUpload(existedStorage, arg, args);
                return InitUploadView.View.builder()
                        .entryName(arg.getEntryName())
                        .success(true)
                        .message("秒传成功")
                        .isSkip(true)
                        .isMultipart(false)
                        .sha256(arg.getSha256())
                        .build();
            }

            String taskKey = getTaskKey(args.getUserId(), arg.getSha256());
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            if (!taskMap.isEmpty()) {
                return buildResumeView(taskMap, args.getUserId(), arg);
            }

            String objectName = generateObjectName(arg.getSha256(), arg.getEntryName());
            if (isDirectUpload(arg.getFileSize())) {
                saveTaskToRedis(taskKey, UPLOAD_TYPE_DIRECT, null, objectName, arg, args);
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

            saveTaskToRedis(taskKey, UPLOAD_TYPE_MULTIPART, uploadId, objectName, arg, args);
            List<String> chunkUrls = generateChunkUrls(uploadId, objectName, arg.getTotalChunks());

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
        validateTaskOwner(taskMap, userId);

        String uploadType = getString(taskMap, FIELD_UPLOAD_TYPE);
        String objectName = getString(taskMap, FIELD_OBJECT_NAME);
        String entryName = getStringOrDefault(taskMap, FIELD_ENTRY_NAME, arg.getEntryName());
        if (UPLOAD_TYPE_DIRECT.equals(uploadType)) {
            String uploadUrl = generateSinglePresignedUrl(getString(taskMap, FIELD_BUCKET_NAME), objectName);
            return InitUploadView.View.builder()
                    .entryName(entryName)
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

        String uploadId = getString(taskMap, FIELD_UPLOAD_ID);
        int totalChunks = getInteger(taskMap, FIELD_TOTAL_CHUNKS);
        List<Integer> uploadedChunks = getUploadedChunkNumbers(userId, arg.getSha256());
        List<String> chunkUrls = regenerateChunkUrls(uploadId, objectName, totalChunks, uploadedChunks);

        return InitUploadView.View.builder()
                .entryName(entryName)
                .success(true)
                .message("大文件分片断点续传")
                .isSkip(false)
                .isMultipart(true)
                .sha256(arg.getSha256())
                .uploadedChunks(uploadedChunks)
                .chunkUrls(chunkUrls)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void createEntryForInstantUpload(Storage storage, InitUploadArgs.Arg arg, InitUploadArgs args) {
        Entry entry = Entry.builder()
                .driveId(args.getDriveId())
                .parentId(args.getParentId())
                .userId(args.getUserId())
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

        int refCount = storageMapper.increaseRefCountBySha256(arg.getSha256());
        if (refCount != 1) {
            throw new BusinessException("秒传增加引用计数失败");
        }

        int driveCount = driveMapper.increaseUsedQuotaById(args.getDriveId(), arg.getFileSize());
        if (driveCount != 1) {
            throw new BusinessException("秒传更新空间配额失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void persistUploadedFile(Map<Object, Object> taskMap, String sha256, Long userId) {
        String entryName = getString(taskMap, FIELD_ENTRY_NAME);
        Long fileSize = getLong(taskMap, FIELD_FILE_SIZE);
        String mimeType = getString(taskMap, FIELD_MIME_TYPE);
        Long driveId = getLong(taskMap, FIELD_DRIVE_ID);
        Long parentId = getLong(taskMap, FIELD_PARENT_ID);
        String bucketName = getString(taskMap, FIELD_BUCKET_NAME);
        String objectName = getString(taskMap, FIELD_OBJECT_NAME);
        String fileExt = getFileSuffix(entryName);

        Storage storage = storageMapper.selectBySha256(sha256);
        Long storageId;
        if (storage != null && Objects.equals(storage.getEnabled(), 1)) {
            int refCount = storageMapper.increaseRefCountBySha256(sha256);
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

        int driveCount = driveMapper.increaseUsedQuotaById(driveId, fileSize);
        if (driveCount != 1) {
            throw new BusinessException("空间配额更新失败");
        }
    }

    private void saveTaskToRedis(String taskKey, String uploadType, String uploadId,
                                 String objectName, InitUploadArgs.Arg arg, InitUploadArgs args) {
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
        task.put(FIELD_USER_ID, String.valueOf(args.getUserId()));
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
                .map(String::valueOf)
                .filter(this::isInteger)
                .map(Integer::parseInt)
                .sorted()
                .toList();
    }

    private List<Part> buildSortedParts(Map<Object, Object> chunksMap) {
        return chunksMap.entrySet().stream()
                .map(e -> new Part(Integer.parseInt(String.valueOf(e.getKey())), String.valueOf(e.getValue())))
                .sorted(Comparator.comparingInt(Part::partNumber))
                .toList();
    }

    private List<Integer> getMissingChunkNumbers(int totalChunks, Map<Object, Object> chunksMap) {
        Set<Integer> uploaded = new HashSet<>();
        for (Object key : chunksMap.keySet()) {
            String keyStr = String.valueOf(key);
            if (!isInteger(keyStr)) {
                continue;
            }
            uploaded.add(Integer.parseInt(keyStr));
        }

        List<Integer> missing = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            if (!uploaded.contains(i)) {
                missing.add(i);
            }
        }
        return missing;
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

    private void cleanUploadCache(String taskKey, String chunksKey) {
        redisTemplate.delete(taskKey);
        if (chunksKey != null) {
            redisTemplate.delete(chunksKey);
        }
    }

    private boolean isDirectUpload(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new BusinessException("fileSize不合法");
        }
        long threshold = minioConfig.getDirectUploadThreshold() == null ? 10 * 1024 * 1024L : minioConfig.getDirectUploadThreshold();
        return fileSize <= threshold;
    }

    private void validateInitRequest(InitUploadArgs args, Long userId) {
        if (args == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (args.getUserId() == null || !Objects.equals(args.getUserId(), userId)) {
            throw new BusinessException("非法的用户ID");
        }
        if (args.getDriveId() == null) {
            throw new BusinessException("driveId不能为空");
        }
        if (args.getParentId() == null) {
            throw new BusinessException("parentId不能为空");
        }
    }

    private void validateSingleArg(InitUploadArgs.Arg arg) {
        if (arg == null) {
            throw new BusinessException("上传文件参数不能为空");
        }
        if (arg.getEntryName() == null || arg.getEntryName().isBlank()) {
            throw new BusinessException("entryName不能为空");
        }
        if (arg.getSha256() == null || arg.getSha256().isBlank()) {
            throw new BusinessException("sha256不能为空");
        }
        if (arg.getFileSize() == null || arg.getFileSize() <= 0) {
            throw new BusinessException("fileSize不合法");
        }

        if (!isDirectUpload(arg.getFileSize())) {
            if (arg.getTotalChunks() == null || arg.getTotalChunks() <= 0) {
                throw new BusinessException("分片上传必须提供totalChunks");
            }
        }
    }

    private void validateTaskOwner(Map<Object, Object> taskMap, Long userId) {
        String taskUserId = getString(taskMap, FIELD_USER_ID);
        if (!Objects.equals(String.valueOf(userId), taskUserId)) {
            throw new BusinessException("任务不属于当前用户");
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

    private String getString(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new BusinessException("任务字段缺失: " + key);
        }
        return String.valueOf(value);
    }

    private String getStringOrDefault(Map<Object, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private int getInteger(Map<Object, Object> map, String key) {
        String value = getString(map, key);
        if (!isInteger(value)) {
            throw new BusinessException("任务字段格式错误: " + key);
        }
        return Integer.parseInt(value);
    }

    private Long getLong(Map<Object, Object> map, String key) {
        String value = getString(map, key);
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            throw new BusinessException("任务字段格式错误: " + key);
        }
    }

    private boolean isInteger(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
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
