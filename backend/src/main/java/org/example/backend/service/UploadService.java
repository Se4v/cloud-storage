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
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.config.MinioConfig;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.mapper.TrafficMapper;
import org.example.backend.model.request.file.UploadInitReq;
import org.example.backend.model.request.file.ChunkMergeReq;
import org.example.backend.model.request.file.DirectUploadReq;
import org.example.backend.model.request.file.ChunkUploadReq;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.entity.Traffic;
import org.example.backend.model.response.file.UploadInitResp;
import org.example.backend.model.response.file.ChunkMergeResp;
import org.example.backend.model.response.file.DirectUploadResp;
import org.example.backend.model.response.file.ChunkUploadResp;
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
    @Lazy
    @Autowired
    private UploadService self;
    private final RedissonClient redissonClient;
    private final MinioConfig minioConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MinioAsyncClient minioAsyncClient;
    private final StorageMapper storageMapper;
    private final EntryMapper entryMapper;
    private final DriveMapper driveMapper;
    private final TrafficMapper trafficMapper;
    private final ConfigService configService;

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

    public UploadService(RedissonClient redissonClient, MinioConfig minioConfig, RedisTemplate<String, Object> redisTemplate,
                         MinioAsyncClient minioAsyncClient, StorageMapper storageMapper, EntryMapper entryMapper,
                         DriveMapper driveMapper, TrafficMapper trafficMapper, ConfigService configService) {
        this.redissonClient = redissonClient;
        this.minioConfig = minioConfig;
        this.redisTemplate = redisTemplate;
        this.minioAsyncClient = minioAsyncClient;
        this.storageMapper = storageMapper;
        this.entryMapper = entryMapper;
        this.driveMapper = driveMapper;
        this.trafficMapper = trafficMapper;
        this.configService = configService;
    }

    public UploadInitResp initUpload(UploadInitReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        List<UploadInitResp.Item> items = new ArrayList<>();
        int successCount = 0;

        if (req.getItems() == null || req.getItems().isEmpty()) {
            return UploadInitResp.builder()
                    .items(items)
                    .totalCount(0)
                    .successCount(0)
                    .build();
        }

        if (!isUploadSizeValid(req)) {
            throw new BusinessException("<UNK>");
        }

        for (UploadInitReq.Item reqItem : req.getItems()) {
            try {
                UploadInitResp.Item respItem = initSingleUpload(reqItem, req, currentUserId);
                if (Boolean.TRUE.equals(respItem.getSuccess())) {
                    successCount++;
                }
                items.add(respItem);
            } catch (Exception e) {
                log.error("初始化上传失败: entryName={}, sha256={}",
                        reqItem == null ? null : reqItem.getEntryName(),
                        reqItem == null ? null : reqItem.getSha256(),
                        e);
                items.add(UploadInitResp.Item.builder()
                        .entryName(reqItem == null ? null : reqItem.getEntryName())
                        .success(false)
                        .message("初始化失败: " + e.getMessage())
                        .build());
            }
        }

        return UploadInitResp.builder()
                .items(items)
                .totalCount(req.getItems().size())
                .successCount(successCount)
                .build();
    }

    public DirectUploadResp directUpload(DirectUploadReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        String taskKey = getTaskKey(currentUserId, req.getSha256());
        Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
        String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
        if (taskMap.isEmpty() || !UPLOAD_TYPE_DIRECT.equals(uploadType)) {
            throw new BusinessException("直传任务不存在或已过期");
        }

        String bucketName = taskMap.get(FIELD_BUCKET_NAME).toString();
        String objectName = taskMap.get(FIELD_OBJECT_NAME).toString();
        ensureObjectExists(bucketName, objectName);

        self.persistUploadedFile(taskMap, req.getSha256(), currentUserId);
        redisTemplate.delete(taskKey);

        String entryName = taskMap.get(FIELD_ENTRY_NAME).toString();
        return DirectUploadResp.builder()
                .entryName(entryName)
                .sha256(req.getSha256())
                .success(true)
                .build();
    }

    public ChunkUploadResp uploadChunk(ChunkUploadReq req) {
        List<ChunkUploadResp.Item> items = new ArrayList<>();
        Long currentUserId = SecurityUtils.getUserId();
        for (ChunkUploadReq.Item item : req.getItems()) {
            try {
                String taskKey = getTaskKey(currentUserId, item.getSha256());
                Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
                if (taskMap.isEmpty()) {
                    throw new BusinessException("任务不存在或已过期");
                }

                String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
                if (!UPLOAD_TYPE_MULTIPART.equals(uploadType)) {
                    throw new BusinessException("当前任务不是分片上传任务");
                }

                if (item.getEtag() == null || item.getEtag().isBlank()) throw new BusinessException("");

                String chunksKey = getChunksKey(currentUserId, item.getSha256());
                Object existingEtag = redisTemplate.opsForHash().get(chunksKey, item.getChunkNumber());
                if (existingEtag == null) {
                    redisTemplate.opsForHash().put(chunksKey, item.getChunkNumber(), item.getEtag());
                    redisTemplate.opsForHash().increment(taskKey, FIELD_UPLOADED_CHUNKS, 1);
                } else if (!Objects.equals(String.valueOf(existingEtag), item.getEtag())) {
                    redisTemplate.opsForHash().put(chunksKey, item.getChunkNumber(), item.getEtag());
                }

                redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
                redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

                String uploadId = taskMap.get(FIELD_UPLOAD_ID).toString();
                items.add(ChunkUploadResp.Item.builder()
                        .uploadId(uploadId)
                        .chunkNumber(item.getChunkNumber())
                        .success(true)
                        .build());
            } catch (Exception e) {
                log.error("记录分片失败: sha256={}, chunk={}",
                        item == null ? null : item.getSha256(),
                        item == null ? null : item.getChunkNumber(),
                        e);
                items.add(ChunkUploadResp.Item.builder()
                        .chunkNumber(item == null ? null : item.getChunkNumber())
                        .success(false)
                        .build());
            }
        }

        return ChunkUploadResp.builder().items(items).build();
    }

    public ChunkMergeResp mergeChunks(ChunkMergeReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        String taskKey = getTaskKey(currentUserId, req.getSha256());
        String chunksKey = getChunksKey(currentUserId, req.getSha256());

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
            self.persistUploadedFile(taskMap, req.getSha256(), currentUserId);
            redisTemplate.delete(taskKey);
            redisTemplate.delete(chunksKey);

            return ChunkMergeResp.builder()
                    .sha256(req.getSha256())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("合并分片失败: sha256={}", req.getSha256(), e);
            return ChunkMergeResp.builder()
                    .sha256(req.getSha256())
                    .success(false)
                    .build();
        }
    }

    private UploadInitResp.Item initSingleUpload(UploadInitReq.Item item, UploadInitReq req, Long userId) throws Exception {
        String lockKey = getLockKey(userId, item.getSha256());
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            String ext = getFileSuffix(item.getEntryName());
            if (configService.getFileTypeBlacklist().contains(ext)) {
                throw new BusinessException("禁止上传该类型文件");
            }

            LambdaQueryWrapper<Storage> storageQuery = new LambdaQueryWrapper<>();
            storageQuery.eq(Storage::getSha256, item.getSha256());
            Storage existedStorage = storageMapper.selectOne(storageQuery);
            if (existedStorage != null) {
                self.createEntryForInstantUpload(existedStorage, item, req, userId);
                return UploadInitResp.Item.builder()
                        .entryName(item.getEntryName())
                        .success(true)
                        .message("秒传成功")
                        .isSkip(true)
                        .isMultipart(false)
                        .sha256(item.getSha256())
                        .build();
            }

            String taskKey = getTaskKey(userId, item.getSha256());
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            if (!taskMap.isEmpty()) {
                return buildResumeView(taskMap, userId, item);
            }

            String objectName = generateObjectName(item.getSha256(), item.getEntryName());
            if (item.getFileSize() <= UPLOAD_THRESHOLD) {
                saveTaskToRedis(taskKey, UPLOAD_TYPE_DIRECT, null, objectName, item, req, userId);
                String uploadUrl = generateSinglePresignedUrl(minioConfig.getBucketName(), objectName);
                return UploadInitResp.Item.builder()
                        .entryName(item.getEntryName())
                        .success(true)
                        .message("获取小文件直传链接成功")
                        .isSkip(false)
                        .isMultipart(false)
                        .sha256(item.getSha256())
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

            saveTaskToRedis(taskKey, UPLOAD_TYPE_MULTIPART, uploadId, objectName, item, req, userId);
            List<String> chunkUrls = generateChunkUrls(uploadId, objectName, item.getTotalChunks());

            // 初始化分片记录表
            // String chunksKey = getChunksKey(args.getUserId(), arg.getSha256());
            // redisTemplate.opsForHash().putAll(chunksKey, new HashMap<>());
            // redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

            return UploadInitResp.Item.builder()
                    .entryName(item.getEntryName())
                    .success(true)
                    .message("大文件分片上传初始化成功")
                    .isSkip(false)
                    .isMultipart(true)
                    .sha256(item.getSha256())
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

    private UploadInitResp.Item buildResumeView(Map<Object, Object> taskMap, Long userId, UploadInitReq.Item item) throws Exception {
        String uploadType = taskMap.get(FIELD_UPLOAD_TYPE).toString();
        String objectName = taskMap.get(FIELD_OBJECT_NAME).toString();
        String bucketName = taskMap.get(FIELD_BUCKET_NAME).toString();

        if (UPLOAD_TYPE_DIRECT.equals(uploadType)) {
            String uploadUrl = generateSinglePresignedUrl(bucketName, objectName);
            return UploadInitResp.Item.builder()
                    .entryName(item.getEntryName())
                    .success(true)
                    .message("小文件断点续传")
                    .isSkip(false)
                    .isMultipart(false)
                    .sha256(item.getSha256())
                    .uploadUrl(uploadUrl)
                    .build();
        }

        if (!UPLOAD_TYPE_MULTIPART.equals(uploadType)) {
            throw new BusinessException("未知上传类型: " + uploadType);
        }

        String uploadId = taskMap.get(FIELD_UPLOAD_ID).toString();
        int totalChunks = Integer.parseInt(taskMap.get(FIELD_TOTAL_CHUNKS).toString());
        List<Integer> uploadedChunks = getUploadedChunkNumbers(userId, item.getSha256());
        List<String> chunkUrls = regenerateChunkUrls(uploadId, objectName, totalChunks, uploadedChunks);

        return UploadInitResp.Item.builder()
                .entryName(item.getEntryName())
                .success(true)
                .message("大文件分片断点续传")
                .isSkip(false)
                .isMultipart(true)
                .sha256(item.getSha256())
                .uploadedChunks(uploadedChunks)
                .chunkUrls(chunkUrls)
                .build();
    }

    @Transactional
    public void createEntryForInstantUpload(Storage storage, UploadInitReq.Item item, UploadInitReq req, Long userId) {
        Entry entry = Entry.builder()
                .driveId(req.getDriveId())
                .parentId(req.getParentId())
                .userId(userId)
                .storageId(storage.getId())
                .entryName(item.getEntryName())
                .entryType(1)
                .fileSize(item.getFileSize())
                .fileExt(getFileSuffix(item.getEntryName()))
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
        driveUpdate.setIncrBy(Drive::getUsedQuota, item.getFileSize()).eq(Drive::getId, req.getDriveId());
        int driveCount = driveMapper.update(driveUpdate);
        if (driveCount != 1) {
            throw new BusinessException("秒传更新空间配额失败");
        }

        Traffic traffic = Traffic.builder()
                .userId(userId)
                .storageId(storage.getId())
                .type(1) // 上传
                .fileSize(item.getFileSize())
                .status(1) // 成功
                .build();
        trafficMapper.insert(traffic);
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
        if (storage != null) {
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

        Traffic traffic = Traffic.builder()
                .userId(userId)
                .storageId(storageId)
                .type(1) // 上传
                .fileSize(fileSize)
                .status(1) // 成功
                .build();
        trafficMapper.insert(traffic);
    }

    private void saveTaskToRedis(String taskKey, String uploadType, String uploadId,
                                 String objectName, UploadInitReq.Item item, UploadInitReq req, Long userId) {
        int totalChunks = (item.getTotalChunks() == null || item.getTotalChunks() <= 0) ? 1 : item.getTotalChunks();
        Map<String, String> task = new HashMap<>();
        task.put(FIELD_UPLOAD_TYPE, uploadType);
        if (uploadId != null) {
            task.put(FIELD_UPLOAD_ID, uploadId);
        }
        task.put(FIELD_ENTRY_NAME, item.getEntryName());
        task.put(FIELD_FILE_SIZE, String.valueOf(item.getFileSize()));
        task.put(FIELD_MIME_TYPE, item.getMimeType() == null ? "application/octet-stream" : item.getMimeType());
        task.put(FIELD_UPLOADED_CHUNKS, "0");
        task.put(FIELD_TOTAL_CHUNKS, String.valueOf(totalChunks));
        task.put(FIELD_USER_ID, String.valueOf(userId));
        task.put(FIELD_DRIVE_ID, String.valueOf(req.getDriveId()));
        task.put(FIELD_PARENT_ID, String.valueOf(req.getParentId()));
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

    private boolean isUploadSizeValid(UploadInitReq req) {
        long totalUploadSize = req.getItems().stream()
                .mapToLong(item -> item.getFileSize() != null ? item.getFileSize() : 0)
                .sum();

        Drive drive = driveMapper.selectById(req.getDriveId());
        long remainingSpace = drive.getTotalQuota() - drive.getUsedQuota();

        return totalUploadSize <= remainingSpace;
    }
}
