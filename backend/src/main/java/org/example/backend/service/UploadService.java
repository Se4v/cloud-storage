package org.example.backend.service;

import io.minio.*;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.config.MinioConfig;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.args.RecordChunksArgs;
import org.example.backend.model.args.DirectUploadArgs;
import org.example.backend.model.args.InitUploadArgs;
import org.example.backend.model.view.MergeChunksView;
import org.example.backend.model.view.RecordChunksView;
import org.example.backend.model.view.DirectUploadView;
import org.example.backend.model.view.InitUploadView;
import org.example.backend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UploadService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MinioConfig minioConfig;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MinioAsyncClient minioClient;

    private static final String TASK_KEY_PREFIX = "upload:task:";
    private static final String CHUNKS_KEY_PREFIX = "upload:chunks:";
    private static final String USER_TASKS_PREFIX = "upload:user:";
    private static final String HASH_INDEX_PREFIX = "upload:hash:";
    private static final long TASK_EXPIRE_HOURS = 24;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public InitUploadView initUpload(InitUploadArgs args, Long userId) {
        // 判断操作是否为同一用户
        if (!args.getUserId().equals(userId)) {
            throw new BusinessException("Invalid user id");
        }

        List<InitUploadView.View> viewList = new ArrayList<>();

        // 计算总上传文件大小
        // Long totalSize = request.getRequestItems() == null ? 0L :
        //         request.getRequestItems().stream()
        //                 .mapToLong(InitUploadRequest.RequestItem::getFileSize)
        //                 .sum();

        // 空间容量判断
        // TODO: 应该在文件上传和分片合并时判断容量

        int successCount = 0;

        // 单个文件上传初始化
        for (InitUploadArgs.Arg arg : args.getArgList()) {
            try {
                InitUploadView.View view = initSingleUpload(arg, args);

                if (view.getSuccess()) {
                    successCount++;
                }

                viewList.add(view);
            } catch (Exception e) {
                viewList.add(InitUploadView.View.builder()
                        .entryName(arg.getEntryName())
                        .success(false)
                        .message("初始化失败")
                        .build());
            }
        }

        return InitUploadView.builder()
                .viewList(viewList)
                .totalCount(args.getArgList().size())
                .successCount(successCount)
                .build();
    }

    private InitUploadView.View initSingleUpload(InitUploadArgs.Arg arg,
                                                         InitUploadArgs args) throws Exception {
        // 秒传检查
        Storage existStorage = fileRepository.findBlobBySha256(arg.getSha256());
        if (existStorage != null && existStorage.getEnabled() == 1) {
            Entry entry = Entry.builder()
                    .driveId(args.getDriveId())
                    .parentId(args.getParentId())
                    .userId(args.getUserId())
                    .storageId(existStorage.getId())
                    .entryName(arg.getEntryName())
                    .entryType(1)
                    .fileSize(arg.getFileSize())
                    .fileExt(existStorage.getFileExt())
                    .enabled(1)
                    .deleted(0)
                    .updaterId(args.getUserId())
                    .build();

            fileRepository.saveEntryWithIncreaseRefCount(entry, arg.getSha256());

            return InitUploadView.View.builder()
                    .entryName(arg.getEntryName())
                    .success(true)
                    .message("秒传成功")
                    .skipUpload(true)
                    .build();
        }

        // 断点续传检查
        String existUploadId = findExistingTask(args.getUserId(), arg.getSha256());
        if (existUploadId != null && validateTaskOwnership(existUploadId, args.getUserId())) {
            List<Integer> uploadedChunks = getUploadedChunksFromRedis(existUploadId);
            List<String> chunkUrls = regenerateChunkUrls(existUploadId, arg.getTotalChunks(), uploadedChunks);

            return InitUploadView.View.builder()
                    .entryName(arg.getEntryName())
                    .success(true)
                    .message("断点续传")
                    .skipUpload(false)
                    .directUpload(false)
                    .sha256(arg.getSha256())
                    .uploadId(existUploadId)
                    .uploadedChunks(uploadedChunks)
                    .chunkUrls(chunkUrls)
                    .build();
        }

        // 小文件上传
        if (arg.getFileSize() <= minioConfig.getDirectUploadThreshold()) {
            return InitUploadView.View.builder()
                    .entryName(arg.getEntryName())
                    .success(true)
                    .message("小文件直传")
                    .skipUpload(false)
                    .directUpload(true)
                    .sha256(arg.getSha256())
                    .build();
        }

        // 大文件上传
        try {
            String uploadId = createNewMultipartTask(arg, args);
            List<String> chunkUrls = generateChunkUrls(uploadId, arg.getTotalChunks());

            return InitUploadView.View.builder()
                    .entryName(arg.getEntryName())
                    .success(true)
                    .message("大文件分片上传")
                    .skipUpload(false)
                    .directUpload(false)
                    .sha256(arg.getSha256())
                    .uploadId(uploadId)
                    .uploadedChunks(List.of())
                    .chunkUrls(chunkUrls)
                    .build();
        } catch (Exception e) {
            throw new BusinessException("大文件分片上传初始化失败");
        }
    }

    private String findExistingTask(Long userId, String sha256) {
        String hashKey = HASH_INDEX_PREFIX + userId + ":" + sha256;

        if (redisTemplate.hasKey(hashKey)) {
            return (String) redisTemplate.opsForHash().get(hashKey, "uploadId");
        }

        return null;
    }

    private boolean validateTaskOwnership(String uploadId, Long userId) {
        String taskKey = TASK_KEY_PREFIX + uploadId;
        String ownerId = (String) redisTemplate.opsForHash().get(taskKey, "userId");

        if (ownerId == null) {
            return false;
        }

        return String.valueOf(userId).equals(ownerId);
    }

    private String createNewMultipartTask(InitUploadArgs.Arg arg,
                                          InitUploadArgs args) throws Exception {
        String objectName = generateObjectName(arg.getSha256(), arg.getEntryName());

        CompletableFuture<CreateMultipartUploadResponse> response = minioClient.createMultipartUploadAsync(
                minioConfig.getBucketName(), null, objectName, null, null);

        String uploadId = response.get().result().uploadId();
        String taskKey = TASK_KEY_PREFIX + uploadId;

        // 任务信息
        Map<String, Object> task = new HashMap<>();
        task.put("entryName", arg.getEntryName());
        task.put("sha256", arg.getSha256());
        task.put("fileSize", arg.getFileSize());
        task.put("chunkSize", arg.getChunkSize());
        task.put("uploadedChunks", "0");
        task.put("totalChunks", arg.getTotalChunks());
        task.put("driveId", args.getDriveId());
        task.put("parentId", args.getParentId());
        task.put("userId", args.getUserId());
        task.put("status", "UPLOADING");
        task.put("bucketName", minioConfig.getBucketName());
        task.put("objectName", objectName);
        task.put("createTime", LocalDateTime.now().toString());

        // TODO:需要合并操作到同一事务

        redisTemplate.opsForHash().putAll(taskKey, task);
        redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

        // 初始化分片信息
        String chunksKey = CHUNKS_KEY_PREFIX + uploadId;
        redisTemplate.opsForHash().putAll(chunksKey, new HashMap<>());
        redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

        // 用户任务索引
        String userTasksKey = USER_TASKS_PREFIX + args.getUserId();
        redisTemplate.opsForSet().add(userTasksKey, uploadId);
        redisTemplate.expire(userTasksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

        // hash索引
        String hashKey = HASH_INDEX_PREFIX + args.getUserId() + ":" + arg.getSha256();
        redisTemplate.opsForHash().putAll(hashKey, Map.of(
                "uploadId", uploadId,
                "status", "UPLOADING",
                "objectName", objectName)
        );
        redisTemplate.expire(hashKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

        return uploadId;
    }

    private List<String> generateChunkUrls(String uploadId, int totalChunks) throws Exception {
        String taskKey = TASK_KEY_PREFIX + uploadId;
        String bucketName = (String) redisTemplate.opsForHash().get(taskKey, "bucketName");
        String objectName = (String) redisTemplate.opsForHash().get(taskKey, "objectName");

        List<String> chunkUrls = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .extraQueryParams(Map.of(
                                    "uploadId", uploadId,
                                    "partNumber", String.valueOf(i)
                            ))
                            .build()
            );
            chunkUrls.add(url);
        }

        return chunkUrls;
    }

    private String generateObjectName(String sha256, String entryName) {
        if (sha256 == null || sha256.length() < 4) {
            throw new BusinessException("SHA256 must be at least 4 characters");
        }
        if (entryName == null || entryName.isBlank()) {
            throw new BusinessException("Entry name must not be empty");
        }

        return LocalDateTime.now().format(DATE_FORMATTER) +
                "/" +
                sha256.substring(0, 4) +
                "/" +
                System.currentTimeMillis() +
                "_" +
                entryName;
    }

    private List<Integer> getUploadedChunksFromRedis(String uploadId) {
        String chunksKey = CHUNKS_KEY_PREFIX + uploadId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(chunksKey);
        return entries.keySet().stream()
                .map(k -> Integer.parseInt(k.toString()))
                .toList();
    }

    private List<String> regenerateChunkUrls(String uploadId, int totalChunks,
                                             List<Integer> excludeChunks) throws Exception {
        Set<Integer> excludeSet = new HashSet<>(excludeChunks);
        List<String> chunkUrls = new ArrayList<>();

        for (int i = 1; i <= totalChunks; i++) {
            if (excludeSet.contains(i)) {
                chunkUrls.add(null); // 已上传的返回null
            } else {
                chunkUrls.add(generateChunkUrls(uploadId, totalChunks).get(i - 1));
            }
        }

        return chunkUrls;
    }

    public DirectUploadView directUpload(DirectUploadArgs args, Long userId) {
        // 判断操作是否为同一用户
        if (!args.getUserId().equals(userId)) {
            throw new BusinessException("Invalid user id");
        }

        String objectName = generateObjectName(args.getSha256(), args.getEntryName());
        String fileExt = getFileSuffix(args.getEntryName());

        try (InputStream inputStream = args.getFile().getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, args.getFileSize(), -1)
                            .contentType(args.getMimeType())
                            .build()
            );

            Entry entry = Entry.builder()
                    .driveId(args.getDriveId())
                    .userId(args.getUserId())
                    .parentId(args.getParentId())
                    .entryName(args.getEntryName())
                    .entryType(1)
                    .fileSize(args.getFileSize())
                    .fileExt(fileExt)
                    .enabled(1)
                    .deleted(0)
                    .updaterId(args.getUserId())
                    .build();

            Storage storage = Storage.builder()
                    .originalName(args.getEntryName())
                    .fileExt(fileExt)
                    .fileSize(args.getFileSize())
                    .sha256(args.getSha256())
                    .bucketName(minioConfig.getBucketName())
                    .objectKey(objectName)
                    .mimeType(args.getMimeType())
                    .enabled(1)
                    .refCount(0)
                    .build();

            fileRepository.saveEntryAndBlobWithUpdateDriveQuota(entry, storage);
        } catch (Exception e) {
            log.error("");

            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .build()
                );
            } catch (InsufficientDataException | InternalException | InvalidKeyException | IOException |
                     NoSuchAlgorithmException | XmlParserException | IllegalArgumentException ex) {
                log.error("");
            }

            throw new BusinessException("");
        }

        return DirectUploadView.builder()
                .entryName(args.getEntryName())
                .sha256(args.getSha256())
                .success(true)
                .build();
    }

    private String getFileSuffix(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }

        // 找到最后一个小数点的位置
        int lastDotIndex = fileName.lastIndexOf('.');

        // 小数点必须存在且不能是第一个和最后一个字符
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        return "";
    }

    public RecordChunksView recordChunks(RecordChunksArgs args, Long userId) {
        List<RecordChunksView.View> viewList = new ArrayList<>();

        for (RecordChunksArgs.Arg arg : args.getArgList()) {
            try {
                if (!validateTaskOwnership(arg.getUploadId(), userId)) {
                    log.warn("");
                    continue;
                }

                RecordChunksView.View view = recordSingleChunk(arg);

                viewList.add(view);
            } catch (Exception e) {
                log.error("");
            }
        }

        return RecordChunksView.builder()
                .viewList(viewList)
                .build();
    }

    private RecordChunksView.View recordSingleChunk(RecordChunksArgs.Arg arg) throws Exception {
        String chunksKey = CHUNKS_KEY_PREFIX + arg.getUploadId();
        String taskKey = TASK_KEY_PREFIX + arg.getUploadId();

        // TODO:需要合并操作到同一事务

        // 记录分片完成
        redisTemplate.opsForHash().put(chunksKey, String.valueOf(arg.getChunkNumber()),
                arg.getEtag() != null ? arg.getEtag() : "completed");

        // 增加已上传分片计数
        redisTemplate.opsForHash().increment(taskKey, "uploadedChunks", 1);

        // 刷新过期时间
        redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

        return RecordChunksView.View.builder()
                .uploadId(arg.getUploadId())
                .chunkNumber(arg.getChunkNumber())
                .success(true)
                .build();
    }

    public MergeChunksView mergeChunks(String uploadId, Long userId) {
        if (!validateTaskOwnership(uploadId, userId)) {
            log.warn("");
            throw new BusinessException("Invalid task ownership");
        }

        try {
            String taskKey = TASK_KEY_PREFIX + uploadId;
            String chunksKey = CHUNKS_KEY_PREFIX + uploadId;

            // 获取任务信息
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            if (taskMap.isEmpty()) {
                throw new BusinessException("上传任务不存在或已过期");
            }

            int totalChunks = Integer.parseInt((String) taskMap.get("totalChunks"));
            int uploadedChunks = Integer.parseInt((String) taskMap.get("uploadedChunks"));

            // 校验分片完整性
            if (uploadedChunks < totalChunks) {
                throw new BusinessException("分片未上传完整，无法合并");
            }

            // 获取所有分片ETag
            Map<Object, Object> chunksMap = redisTemplate.opsForHash().entries(chunksKey);
            List<Part> parts = chunksMap.entrySet().stream()
                    .map(e -> new Part(
                            Integer.parseInt(e.getKey().toString()),
                            e.getValue().toString()))
                    .sorted(Comparator.comparingInt(Part::partNumber))
                    .toList();

            if (parts.size() != totalChunks) {
                throw new BusinessException("分片记录与实际不符");
            }

            // 调用MinIO合并
            String bucketName = (String) taskMap.get("bucketName");
            String objectName = (String) taskMap.get("objectName");

            minioClient.completeMultipartUploadAsync(
                    bucketName,
                    null,
                    objectName,
                    uploadId,
                    parts.toArray(new Part[0]),
                    null,
                    null
            );

            Long driveId = (Long) taskMap.get("driveId");
            Long parentId = (Long) taskMap.get("parentId");
            String entryName = (String) taskMap.get("entryName");
            String sha256 = (String) taskMap.get("sha256");
            Long fileSize = (Long) taskMap.get("fileSize");
            String mimeType = (String) taskMap.get("mimeType");
            String fileExt = getFileSuffix(entryName);

            // 写入元数据
            Entry entry = Entry.builder()
                    .driveId(driveId)
                    .userId(userId)
                    .parentId(parentId)
                    .entryName(entryName)
                    .entryType(1)
                    .fileSize(fileSize)
                    .fileExt(fileExt)
                    .enabled(1)
                    .deleted(0)
                    .updaterId(userId)
                    .build();

            Storage storage = Storage.builder()
                    .originalName(entryName)
                    .fileExt(fileExt)
                    .fileSize(fileSize)
                    .sha256(sha256)
                    .bucketName(minioConfig.getBucketName())
                    .objectKey(objectName)
                    .mimeType(mimeType)
                    .enabled(1)
                    .refCount(0)
                    .build();

            fileRepository.saveEntryAndBlobWithUpdateDriveQuota(entry, storage);

            // 更新Redis状态
            String hashKey = HASH_INDEX_PREFIX + userId + ":" + sha256;
            redisTemplate.opsForHash().put(taskKey, "status", "COMPLETED");
            redisTemplate.opsForHash().put(hashKey, "status", "COMPLETED");

            // 清理过期数据（保留1小时用于查询）
            redisTemplate.expire(taskKey, 1, TimeUnit.HOURS);
            redisTemplate.expire(chunksKey, 1, TimeUnit.HOURS);

            // 从用户任务列表中移除（Set）
            String userTasksKey = USER_TASKS_PREFIX + userId;
            redisTemplate.opsForSet().remove(userTasksKey, uploadId);
        } catch (Exception e) {
            log.error("");
            return MergeChunksView.builder()
                    .uploadId(uploadId)
                    .success(false)
                    .build();
        }

        return MergeChunksView.builder()
                .uploadId(uploadId)
                .success(true)
                .build();
    }
}
