package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.example.backend.aspect.LogContextHolder;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.mapper.TrafficMapper;
import org.example.backend.model.request.file.EntryDownloadReq;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.entity.Traffic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DownloadService {
    private final MinioClient minioClient;
    private final EntryMapper entryMapper;
    private final StorageMapper storageMapper;
    private final TrafficMapper trafficMapper;

    private static final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    public DownloadService(MinioClient minioClient, EntryMapper entryMapper,
                           StorageMapper storageMapper, TrafficMapper trafficMapper) {
        this.minioClient = minioClient;
        this.entryMapper = entryMapper;
        this.storageMapper = storageMapper;
        this.trafficMapper = trafficMapper;
    }

    /**
     * 文件下载
     * @param req 下载请求参数（文件/文件夹ID列表）
     * @return 流式响应体
     */
    public StreamingResponseBody download(EntryDownloadReq req, boolean needTrafficRecord) {
        return outputStream -> {
            try {
                List<DownloadTask> tasks = collectDownloadTasks(req.getIds());
                if (tasks.isEmpty()) {
                    throw new BusinessException("文件不存在");
                }

                if (needTrafficRecord) {
                    // 计算总大小并记录流量
                    long totalSize = tasks.stream()
                            .filter(task -> !task.isEmptyDir())
                            .mapToLong(task -> task.size)
                            .sum();

                    Long currentUserId = SecurityUtils.getUserId();
                    Traffic traffic = Traffic.builder()
                            .userId(currentUserId)
                            .storageId(0L)
                            .type(2)
                            .fileSize(totalSize)
                            .status(1)
                            .build();
                    trafficMapper.insert(traffic);
                }

                if (tasks.size() == 1 && !tasks.get(0).isEmptyDir()) {
                    DownloadTask task = tasks.get(0);
                    try (InputStream is = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(task.bucketName)
                                    .object(task.objectKey)
                                    .build())) {
                        is.transferTo(outputStream);
                    }
                } else {
                    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
                        Set<String> entryNames = new HashSet<>();
                        for (DownloadTask task : tasks) {
                            writeToZip(zipOutputStream, task, entryNames);
                        }
                        zipOutputStream.finish();
                    }
                }
            } catch (Exception e) {
                logger.error("下载失败: {}", e.getMessage());
                throw new BusinessException("下载失败");
            }
        };
    }

    /**
     * 获取下载文件的名称
     * @param req 下载请求参数
     * @return 文件名（单文件=原名，多文件=时间戳.zip）
     */
    public String getDownloadFileName(EntryDownloadReq req) {
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED)
                        .in(Entry::getId, req.getIds()));

        if (entries == null || entries.isEmpty()) {
            throw new BusinessException("文件不存在");
        }

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("批量下载" + entries.size() + "个文件");
        List<Map<String, Object>> files = entries.stream()
                .map(file -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", file.getId());
                    map.put("name", file.getEntryName());
                    map.put("type", file.getEntryType());
                    return map;
                })
                .toList();
        LogContextHolder.addDetailProperty("batch_download", files);

        // 单文件下载：直接返回文件名
        if (entries.size() == 1 && entries.get(0).getEntryType() == DbConsts.ENTRY_TYPE_FILE) {
            return entries.get(0).getEntryName();
        }

        // 多文件/文件夹下载：返回zip文件名
        return "file-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".zip";
    }

    /**
     * 获取文件的content-type
     * @param req 下载请求参数
     * @return content-type
     */
    public String getFileContentType(EntryDownloadReq req) {
        Entry entry = entryMapper.selectById(req.getIds().get(0));
        if (entry == null) return "application/octet-stream";
        Storage storage = storageMapper.selectById(entry.getStorageId());
        return storage.getMimeType();
    }

    /**
     * 收集下载任务
     * @param entryIds 选中的文件/文件夹ID
     * @return 下载任务列表
     */
    private List<DownloadTask> collectDownloadTasks(List<Long> entryIds) {
        List<DownloadTask> tasks = new ArrayList<>();

        // 批量查询根节点（状态为未删除）
        List<Entry> roots = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .in(Entry::getId, entryIds)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));

        if (roots == null || roots.isEmpty()) {
            return tasks;
        }

        // 单文件直接返回，无需查子节点
        if (roots.size() == 1 && roots.get(0).getEntryType() == DbConsts.ENTRY_TYPE_FILE) {
            Entry file = roots.get(0);
            Storage storage = storageMapper.selectById(file.getStorageId());
            if (storage != null) {
                tasks.add(new DownloadTask(file.getEntryName(), storage.getBucketName(),
                        storage.getObjectKey(), false, storage.getFileSize()));
            }
            return tasks;
        }

        // 收集文件夹ID，递归查询所有后代节点
        List<Long> folderIds = roots.stream()
                .filter(e -> e.getEntryType() == DbConsts.ENTRY_TYPE_FOLDER)
                .map(Entry::getId)
                .toList();

        Map<Long, List<Entry>> childrenMap = new HashMap<>();
        if (!folderIds.isEmpty()) {
            // 递归查询所有后代节点，按父ID分组
            List<Entry> descendants = entryMapper.selectDescendantsByFolderId(folderIds);
            childrenMap = descendants.stream().collect(Collectors.groupingBy(Entry::getParentId));
        }

        // 批量构建存储信息映射
        Map<Long, Storage> storageMap = buildStorageMap(roots, childrenMap);

        // 遍历根节点，生成完整下载任务
        for (Entry root : roots) {
            String rootPath = root.getEntryName();
            if (root.getEntryType() == DbConsts.ENTRY_TYPE_FOLDER) {
                // 递归遍历文件夹，生成带层级路径的任务
                buildTasks(root, rootPath, childrenMap, storageMap, tasks);
            } else {
                // 普通文件直接构建任务
                Storage storage = storageMap.get(root.getStorageId());
                if (storage != null) {
                    tasks.add(new DownloadTask(root.getEntryName(), storage.getBucketName(),
                            storage.getObjectKey(), false, storage.getFileSize()));
                }
            }
        }

        return tasks;
    }

    /**
     * 构建存储ID映射表
     * @param roots 根节点列表
     * @param childrenMap 子节点映射
     * @return storageId -> Storage
     */
    private Map<Long, Storage> buildStorageMap(List<Entry> roots, Map<Long, List<Entry>> childrenMap) {
        Set<Long> storageIds = new HashSet<>();

        // 收集根节点中的文件 storageId
        for (Entry root : roots) {
            if (root.getEntryType() == DbConsts.ENTRY_TYPE_FILE && root.getStorageId() != null && root.getStorageId() != 0) {
                storageIds.add(root.getStorageId());
            }
        }

        // 收集后代节点中的文件 storageId
        for (List<Entry> children : childrenMap.values()) {
            for (Entry child : children) {
                if (child.getEntryType() == DbConsts.ENTRY_TYPE_FILE && child.getStorageId() != null && child.getStorageId() != 0) {
                    storageIds.add(child.getStorageId());
                }
            }
        }

        if (storageIds.isEmpty()) return new HashMap<>();

        // 批量查询 Storage 记录，构建 storageId -> Storage 映射
        List<Storage> storages = storageMapper.selectList(
                Wrappers.<Storage>lambdaQuery().in(Storage::getId, storageIds));

        return storages.stream().collect(Collectors.toMap(Storage::getId, s -> s));
    }

    /**
     * 递归构建下载任务
     * @param node 当前节点
     * @param currentPath 当前路径（用于维持ZIP内的目录结构）
     * @param childrenMap parentId -> 子节点列表 的映射
     * @param storageMap storageId -> Storage 映射
     * @param tasks 下载任务列表
     */
    private void buildTasks(Entry node, String currentPath,
                            Map<Long, List<Entry>> childrenMap,
                            Map<Long, Storage> storageMap,
                            List<DownloadTask> tasks) {
        if (node.getEntryType() == DbConsts.ENTRY_TYPE_FOLDER) {
            tasks.add(DownloadTask.emptyDir(currentPath + "/"));

            List<Entry> children = childrenMap.getOrDefault(node.getId(), Collections.emptyList());
            for (Entry child : children) {
                String childPath = currentPath + "/" + child.getEntryName();
                if (child.getEntryType() == DbConsts.ENTRY_TYPE_FOLDER) {
                    buildTasks(child, childPath, childrenMap, storageMap, tasks);
                } else {
                    Storage storage = storageMap.get(child.getStorageId());
                    if (storage != null) {
                        tasks.add(new DownloadTask(childPath, storage.getBucketName(),
                                storage.getObjectKey(), false, storage.getFileSize()));
                    }
                }
            }
        }
    }

    /**
     * 将文件写入ZIP压缩包
     * @param zos ZIP输出流
     * @param task 下载任务
     * @param entryNames 已存在的文件名（去重）
     */
    private void writeToZip(ZipOutputStream zos, DownloadTask task, Set<String> entryNames)
            throws Exception {
        // 获取唯一文件名
        String entryPath = getUniqueEntryName(task.zipPath, entryNames);
        entryNames.add(entryPath);

        ZipEntry entry = new ZipEntry(entryPath);
        // 空目录：仅创建目录结构
        if (task.isEmptyDir()) {
            zos.putNextEntry(entry);
            zos.closeEntry();
            return;
        }

        // 写入文件到ZIP
        zos.putNextEntry(entry);
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(task.bucketName)
                        .object(task.objectKey)
                        .build())) {
            is.transferTo(zos);
        }
        zos.closeEntry();
    }

    /**
     * 生成唯一文件名
     * @param original 原始文件名
     * @param existing 已存在的文件名集合
     * @return 唯一文件名
     */
    private String getUniqueEntryName(String original, Set<String> existing) {
        if (!existing.contains(original)) return original;

        int dotIndex = original.lastIndexOf('.');
        String name = dotIndex > 0 ? original.substring(0, dotIndex) : original;
        String ext = dotIndex > 0 ? original.substring(dotIndex) : "";

        int count = 1;
        String newName;
        do {
            newName = name + "(" + count + ")" + ext;
            count++;
        } while (existing.contains(newName));

        return newName;
    }

    /**
     * 下载任务内部类
     */
    @AllArgsConstructor
    private static class DownloadTask {
        /** ZIP 中的相对路径 */
        String zipPath;
        /** MinIO 存储桶名称 */
        String bucketName;
        /** MinIO 对象键 */
        String objectKey;
        /** 是否为空文件夹 */
        boolean emptyDir;
        long size;

        /** 创建空文件夹任务 */
        static DownloadTask emptyDir(String path) {
            return new DownloadTask(path, null, null, true, 0L);
        }

        boolean isEmptyDir() { return emptyDir; }
    }

}