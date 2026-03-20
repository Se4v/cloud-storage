package org.example.backend.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.config.MinioConfig;
import org.example.backend.model.args.SimpleUploadArgs;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.args.UploadChunkArgs;
import org.example.backend.model.args.InitUploadArgs;
import org.example.backend.model.view.MergeChunksView;
import org.example.backend.model.view.UploadChunkView;
import org.example.backend.model.view.SimpleUploadView;
import org.example.backend.model.view.InitUploadView;
import org.example.backend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
    private MinioAsyncClient minioAsyncClient;

    private static final String TASK_KEY_PREFIX = "upload:task:";
    private static final String CHUNKS_KEY_PREFIX = "upload:chunks:";
    private static final long TASK_EXPIRE_HOURS = 24;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final String UPLOAD_TYPE_DIRECT = "direct";
    private static final String UPLOAD_TYPE_MULTIPART = "multipart";

    /**
     * 获取任务的主键 Key
     */
    private String getTaskKey(Long userId, String sha256) {
        return TASK_KEY_PREFIX + userId + ":" + sha256;
    }

    private String getChunksKey(Long userId, String sha256) {
        return CHUNKS_KEY_PREFIX + userId + ":" + sha256;
    }

    // ================== 路线 1 & 2 & 3: 初始化上传 ==================

    public InitUploadView initUpload(InitUploadArgs args, Long userId) {
        if (!args.getUserId().equals(userId)) {
            throw new BusinessException("非法的用户ID");
        }

        List<InitUploadView.View> viewList = new ArrayList<>();
        int successCount = 0;

        for (InitUploadArgs.Arg arg : args.getArgList()) {
            try {
                InitUploadView.View view = initSingleUpload(arg, args);
                if (view.getSuccess()) {
                    successCount++;
                }
                viewList.add(view);
            } catch (Exception e) {
                log.error("初始化上传失败: 文件={}", arg.getEntryName(), e);
                viewList.add(InitUploadView.View.builder()
                        .entryName(arg.getEntryName())
                        .success(false)
                        .message("初始化失败: " + e.getMessage())
                        .build());
            }
        }

        return InitUploadView.builder().viewList(viewList).totalCount(args.getArgList().size())
                .successCount(successCount).build();
    }

    private InitUploadView.View initSingleUpload(InitUploadArgs.Arg arg, InitUploadArgs args) throws Exception {
        // 【路线一：秒传检查】
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
                    .status(1)
                    .updaterId(args.getUserId())
                    .build();
            fileRepository.saveEntryWithIncreaseRefCount(entry, arg.getSha256());
            return InitUploadView.View.builder().entryName(arg.getEntryName()).success(true)
                    .message("秒传成功").isSkip(true).build();
        }

        String taskKey = getTaskKey(args.getUserId(), arg.getSha256());
        Map<Object, Object> existingTask = redisTemplate.opsForHash().entries(taskKey);

        // 【断点续传检查】(如果有未完成的任务，直接返回剩下的链接)
        if (!existingTask.isEmpty()) {
            String uploadType = existingTask.get("uploadType").toString();
            String objectName = existingTask.get("objectName").toString();

            if (UPLOAD_TYPE_DIRECT.equals(uploadType)) {
                // 小文件续传：重新给一个 PUT 链接
                String uploadUrl = generateSinglePresignedUrl(objectName);
                return InitUploadView.View.builder().entryName(arg.getEntryName()).success(true)
                        .message("小文件断点续传").isSkip(false).isMultipart(false)
                        .uploadUrl(uploadUrl).sha256(arg.getSha256()).build();
            } else {
                // 大文件续传：查出缺失的分片，生成链接
                String uploadId = existingTask.get("uploadId").toString();
                List<Integer> uploadedChunks = getUploadedChunksFromRedis(args.getUserId(), arg.getSha256());
                List<String> chunkUrls = regenerateChunkUrls(uploadId, objectName, arg.getTotalChunks(), uploadedChunks);
                return InitUploadView.View.builder().entryName(arg.getEntryName()).success(true)
                        .message("大文件分片断点续传").isSkip(false).isMultipart(true)
                        .sha256(arg.getSha256()).uploadedChunks(uploadedChunks).chunkUrls(chunkUrls).build();
            }
        }

        // 新建上传任务
        String objectName = generateObjectName(arg.getSha256(), arg.getEntryName());

        // 【路线二：小文件直传初始化】
        if (arg.getFileSize() <= minioConfig.getDirectUploadThreshold()) {
            String uploadUrl = generateSinglePresignedUrl(objectName);
            saveTaskToRedis(taskKey, UPLOAD_TYPE_DIRECT, null, objectName, arg, args);

            return InitUploadView.View.builder().entryName(arg.getEntryName()).success(true)
                    .message("获取小文件直传链接成功").isSkip(false).isMultipart(false)
                    .uploadUrl(uploadUrl).sha256(arg.getSha256()).build();
        }

        // 【路线三：大文件分片初始化】
        CompletableFuture<CreateMultipartUploadResponse> response = minioAsyncClient.createMultipartUploadAsync(
                minioConfig.getBucketName(), null, objectName, null, null);
        String uploadId = response.get().result().uploadId();

        saveTaskToRedis(taskKey, UPLOAD_TYPE_MULTIPART, uploadId, objectName, arg, args);
        List<String> chunkUrls = generateChunkUrls(uploadId, objectName, arg.getTotalChunks());

        // 初始化分片记录表
        String chunksKey = getChunksKey(args.getUserId(), arg.getSha256());
        redisTemplate.opsForHash().putAll(chunksKey, new HashMap<>());
        redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

        return InitUploadView.View.builder().entryName(arg.getEntryName()).success(true)
                .message("大文件分片上传初始化成功").isSkip(false).isMultipart(true)
                .sha256(arg.getSha256()).uploadedChunks(List.of()).chunkUrls(chunkUrls).build();
    }


    // ================== 收尾工作 ==================

    /**
     * 【路线二 收尾】小文件直传完成确认
     */
    public SimpleUploadView simpleUpload(SimpleUploadArgs args, Long userId) {
        String taskKey = getTaskKey(userId, args.getSha256());
        Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);

        if (taskMap.isEmpty() || !UPLOAD_TYPE_DIRECT.equals(taskMap.get("uploadType"))) {
            throw new BusinessException("直传任务不存在或已过期");
        }

        return (SimpleUploadView) saveToDatabaseAndCleanRedis(taskMap, taskKey, null, args.getSha256(), userId, true);
    }

    /**
     * 【路线三 记录进度】大文件分片上传进度记录
     */
    public UploadChunkView uploadChunk(UploadChunkArgs args, Long userId) {
        List<UploadChunkView.View> viewList = new ArrayList<>();

        for (UploadChunkArgs.Arg arg : args.getArgList()) {
            try {
                String chunksKey = getChunksKey(userId, arg.getSha256());
                String taskKey = getTaskKey(userId, arg.getSha256());

                if (!redisTemplate.hasKey(taskKey)) {
                    throw new BusinessException("任务已过期");
                }

                // 记录分片ETag
                redisTemplate.opsForHash().put(chunksKey, String.valueOf(arg.getChunkNumber()), arg.getEtag());
                redisTemplate.opsForHash().increment(taskKey, "uploadedChunks", 1);

                // 刷新过期时间
                redisTemplate.expire(chunksKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
                redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);

                viewList.add(UploadChunkView.View.builder().chunkNumber(arg.getChunkNumber()).success(true).build());
            } catch (Exception e) {
                log.error("记录分片失败: sha256={}, chunk={}", arg.getSha256(), arg.getChunkNumber(), e);
                viewList.add(UploadChunkView.View.builder().chunkNumber(arg.getChunkNumber()).success(false).build());
            }
        }
        return UploadChunkView.builder().viewList(viewList).build();
    }

    /**
     * 【路线三 收尾】大文件合并分片
     */
    public MergeChunksView mergeChunks(String sha256, Long userId) {
        String taskKey = getTaskKey(userId, sha256);
        String chunksKey = getChunksKey(userId, sha256);

        try {
            Map<Object, Object> taskMap = redisTemplate.opsForHash().entries(taskKey);
            if (taskMap.isEmpty() || !UPLOAD_TYPE_MULTIPART.equals(taskMap.get("uploadType"))) {
                throw new BusinessException("分片上传任务不存在或已过期");
            }

            int totalChunks = Integer.parseInt(taskMap.get("totalChunks").toString());
            Map<Object, Object> chunksMap = redisTemplate.opsForHash().entries(chunksKey);

            if (chunksMap.size() < totalChunks) {
                throw new BusinessException("分片未上传完整，无法合并");
            }

            // 构造 MinIO 需要的 Part 列表
            List<Part> parts = chunksMap.entrySet().stream()
                    .map(e -> new Part(Integer.parseInt(e.getKey().toString()), e.getValue().toString()))
                    .sorted(Comparator.comparingInt(Part::partNumber))
                    .toList();

            String bucketName = taskMap.get("bucketName").toString();
            String objectName = taskMap.get("objectName").toString();
            String uploadId = taskMap.get("uploadId").toString();

            // 呼叫 MinIO 合并底层文件
            minioAsyncClient.completeMultipartUploadAsync(
                    bucketName, null, objectName, uploadId, parts.toArray(new Part[0]), null, null
            ).get();

            // 入库并清理 Redis
            saveToDatabaseAndCleanRedis(taskMap, taskKey, chunksKey, sha256, userId, false);

        } catch (Exception e) {
            log.error("合并分片失败: sha256={}", sha256, e);
            return MergeChunksView.builder().success(false).build();
        }

        return MergeChunksView.builder().success(true).build();
    }


    // ================== 私有辅助方法 ==================

    /**
     * 统一处理 MySQL 入库和 Redis 清理
     */
    private Object saveToDatabaseAndCleanRedis(Map<Object, Object> taskMap, String taskKey, String chunksKey, String sha256, Long userId, boolean isDirect) {
        String objectName = taskMap.get("objectName").toString();
        String entryName = taskMap.get("entryName").toString();
        Long fileSize = Long.valueOf(taskMap.get("fileSize").toString());
        String mimeType = taskMap.get("mimeType").toString();
        Long driveId = Long.valueOf(taskMap.get("driveId").toString());
        Long parentId = Long.valueOf(taskMap.get("parentId").toString());
        String fileExt = getFileSuffix(entryName);

        try {
            // 可选校验：检查 MinIO 是否真的有这个文件（防刷接口）
            minioAsyncClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build()).get();

            Entry entry = Entry.builder().driveId(driveId).userId(userId).parentId(parentId)
                    .entryName(entryName).entryType(1).fileSize(fileSize).fileExt(fileExt).status(1).updaterId(userId).build();

            Storage storage = Storage.builder().originalName(entryName).fileExt(fileExt).fileSize(fileSize)
                    .sha256(sha256).bucketName(minioConfig.getBucketName()).objectKey(objectName)
                    .mimeType(mimeType).enabled(1).refCount(0).build();

            fileRepository.saveEntryAndBlobWithUpdateDriveQuota(entry, storage);

            // 成功后删除 Redis 任务
            redisTemplate.delete(taskKey);
            if (chunksKey != null) redisTemplate.delete(chunksKey);

            if (isDirect) {
                return SimpleUploadView.builder().entryName(entryName).sha256(sha256).success(true).build();
            } else {
                return MergeChunksView.builder().success(true).build();
            }

        } catch (Exception e) {
            log.error("文件元数据入库失败: {}", entryName, e);
            throw new BusinessException("文件保存处理失败: " + e.getMessage());
        }
    }

    private void saveTaskToRedis(String taskKey, String uploadType, String uploadId, String objectName,
                                 InitUploadArgs.Arg arg, InitUploadArgs args) {
        Map<String, String> task = new HashMap<>();
        task.put("uploadType", uploadType); // "direct" 或 "multipart"
        if (uploadId != null) task.put("uploadId", uploadId);
        task.put("objectName", objectName);
        task.put("bucketName", minioConfig.getBucketName());
        task.put("entryName", arg.getEntryName());
        task.put("fileSize", String.valueOf(arg.getFileSize()));
        task.put("mimeType", arg.getMimeType());
        task.put("totalChunks", String.valueOf(arg.getTotalChunks()));
        task.put("uploadedChunks", "0");
        task.put("driveId", String.valueOf(args.getDriveId()));
        task.put("parentId", String.valueOf(args.getParentId()));
        task.put("createTime", LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(taskKey, task);
        redisTemplate.expire(taskKey, TASK_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    private String generateSinglePresignedUrl(String objectName) throws Exception {
        return minioAsyncClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(minioConfig.getBucketName())
                        .object(objectName)
                        .expiry(1, TimeUnit.HOURS)
                        .build()
        );
    }

    private List<String> generateChunkUrls(String uploadId, String objectName, int totalChunks) throws Exception {
        List<String> chunkUrls = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            chunkUrls.add(minioAsyncClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .extraQueryParams(Map.of("uploadId", uploadId, "partNumber", String.valueOf(i)))
                            .build()
            ));
        }
        return chunkUrls;
    }

    private List<String> regenerateChunkUrls(String uploadId, String objectName, int totalChunks, List<Integer> uploadedChunks) throws Exception {
        Set<Integer> uploadedSet = new HashSet<>(uploadedChunks);
        List<String> chunkUrls = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            if (uploadedSet.contains(i)) {
                chunkUrls.add(null); // 已上传的分片占位为空
            } else {
                chunkUrls.add(minioAsyncClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .expiry(1, TimeUnit.HOURS)
                                .extraQueryParams(Map.of("uploadId", uploadId, "partNumber", String.valueOf(i)))
                                .build()
                ));
            }
        }
        return chunkUrls;
    }

    private List<Integer> getUploadedChunksFromRedis(Long userId, String sha256) {
        String chunksKey = getChunksKey(userId, sha256);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(chunksKey);
        return entries.keySet().stream().map(k -> Integer.parseInt(k.toString())).sorted().toList();
    }

    private String generateObjectName(String sha256, String entryName) {
        if (sha256 == null || sha256.length() < 4) throw new BusinessException("SHA256格式错误");
        return LocalDateTime.now().format(DATE_FORMATTER) + "/" + sha256.substring(0, 4) + "/" + System.currentTimeMillis() + "_" + entryName;
    }

    private String getFileSuffix(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
