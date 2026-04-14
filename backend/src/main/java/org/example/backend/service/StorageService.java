package org.example.backend.service;

import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.entity.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StorageService {
    private final StorageMapper storageMapper;
    private final MinioClient minioClient;

    private static final int BATCH_SIZE = 1000;
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    public StorageService(StorageMapper storageMapper, MinioClient minioClient) {
        this.storageMapper = storageMapper;
        this.minioClient = minioClient;
    }

    /**
     * 清理存储文件
     * @param storageList 要清理的存储记录列表
     */
    @Transactional
    public void cleanStorageFile(List<Storage> storageList) {
        List<Long> storageIds = storageList.stream()
                .map(Storage::getId)
                .toList();

        int count = storageMapper.deleteByIds(storageIds);
        if (count != storageList.size()) throw new BusinessException("<UNK>");

        batchDeleteByBucket(storageList);
    }

    /**
     * 按桶分组批量删除MinIO中的存储对象
     * @param storageList 要删除的存储记录列表
     */
    public void batchDeleteByBucket(List<Storage> storageList) {
        if (CollectionUtils.isEmpty(storageList)) {
            return;
        }

        // 按 bucketName 分组
        Map<String, List<Storage>> groupByBucket = storageList.stream()
                .collect(Collectors.groupingBy(Storage::getBucketName));

        logger.info("开始批量删除 MinIO 文件，共 {} 个文件，涉及 {} 个桶", storageList.size(), groupByBucket.size());

        // 遍历每个桶执行删除
        for (Map.Entry<String, List<Storage>> entry : groupByBucket.entrySet()) {
            String bucketName = entry.getKey();
            List<Storage> filesInBucket = entry.getValue();

            try {
                // 对同一个桶内的文件进行分批处理（防止单次请求过大）
                for (int i = 0; i < filesInBucket.size(); i += BATCH_SIZE) {
                    // 截取当前批次的数据
                    int end = Math.min(i + BATCH_SIZE, filesInBucket.size());
                    List<Storage> currentBatch = filesInBucket.subList(i, end);

                    // 转换为 DeleteObject 列表
                    List<DeleteObject> deleteObjects = currentBatch.stream()
                            .map(s -> new DeleteObject(s.getObjectKey()))
                            .collect(Collectors.toList());

                    // 执行批量删除
                    Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                            RemoveObjectsArgs.builder()
                                    .bucket(bucketName)
                                    .objects(deleteObjects)
                                    .build()
                    );

                    // 5. 检查删除结果
                    for (Result<DeleteError> result : results) {
                        DeleteError error = result.get();
                        logger.error("删除 MinIO 文件失败: bucket={}, object={}, message={}",
                                bucketName, error.objectName(), error.message());
                    }

                    logger.debug("批次删除成功: bucket={}, 数量={}", bucketName, currentBatch.size());
                }
            } catch (Exception e) {
                logger.error("MinIO 批量删除发生异常: bucket={}", bucketName, e);
            }
        }
    }
}
