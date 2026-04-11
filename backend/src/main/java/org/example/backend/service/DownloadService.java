package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.mapper.TrafficMapper;
import org.example.backend.model.request.file.FileDownloadReq;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.entity.Traffic;
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

@Slf4j
@Service
public class DownloadService {
    private final MinioClient minioClient;
    private final EntryMapper entryMapper;
    private final StorageMapper storageMapper;
    private final TrafficMapper trafficMapper;

    public DownloadService(MinioClient minioClient, EntryMapper entryMapper,
                           StorageMapper storageMapper, TrafficMapper trafficMapper) {
        this.minioClient = minioClient;
        this.entryMapper = entryMapper;
        this.storageMapper = storageMapper;
        this.trafficMapper = trafficMapper;
    }

    private static final int TYPE_FILE = 1;
    private static final int TYPE_FOLDER = 2;
    private static final int STATUS_UNDELETED = 1;

    public StreamingResponseBody download(FileDownloadReq req, Long userId) {
        return outputStream -> {
            try {
                List<DownloadTask> tasks = collectDownloadTasks(req.getIds());
                if (tasks.isEmpty()) {
                    throw new BusinessException("File not found");
                }

                // 计算总大小并记录流量
                long totalSize = tasks.stream()
                        .filter(task -> !task.isEmptyDir())
                        .mapToLong(task -> task.size)
                        .sum();

                Traffic traffic = Traffic.builder()
                        .userId(userId)
                        .storageId(null)
                        .type(2)
                        .fileSize(totalSize)
                        .status(1)
                        .build();
                trafficMapper.insert(traffic);

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
                log.error("Download error: {}", e.getMessage());
                throw new BusinessException("Download failed");
            }
        };
    }

    /**
     * 获取下载文件名（在流式传输前调用）
     */
    public String getDownloadFileName(FileDownloadReq req) {
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getStatus, STATUS_UNDELETED)
                        .in(Entry::getId, req.getIds()));

        if (entries == null || entries.isEmpty()) {
            throw new BusinessException("File not found");
        }

        // 单文件下载：直接返回文件名
        if (entries.size() == 1 && entries.get(0).getEntryType() == TYPE_FILE) {
            return entries.get(0).getEntryName();
        }

        // 多文件/文件夹下载：返回zip文件名
        return "file-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".zip";
    }

    /**
     * 收集所有需要下载的文件和文件夹，构建下载任务列表
     */
    private List<DownloadTask> collectDownloadTasks(List<Long> entryIds) {
        List<DownloadTask> tasks = new ArrayList<>();

        // 1. 批量查询根节点（状态为未删除）
        List<Entry> roots = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .in(Entry::getId, entryIds)
                        .eq(Entry::getStatus, STATUS_UNDELETED));

        if (roots == null || roots.isEmpty()) {
            return tasks;
        }

        // 单文件直接返回，无需查子节点
        if (roots.size() == 1 && roots.get(0).getEntryType() == TYPE_FILE) {
            Entry file = roots.get(0);
            Storage storage = storageMapper.selectById(file.getStorageId());
            if (storage != null) {
                tasks.add(new DownloadTask(file.getEntryName(), storage.getBucketName(),
                        storage.getObjectKey(), false, storage.getFileSize()));
            }
            return tasks;
        }

        // 2. 收集文件夹ID，递归查询所有后代节点
        List<Long> folderIds = roots.stream()
                .filter(e -> e.getEntryType() == TYPE_FOLDER)
                .map(Entry::getId)
                .toList();

        Map<Long, List<Entry>> childrenMap = new HashMap<>();
        if (!folderIds.isEmpty()) {
            // 递归查询子节点，构建 parentId -> children 映射
            List<Entry> descendants = entryMapper.selectDescendantsByFolderId(folderIds);
            childrenMap = descendants.stream().collect(Collectors.groupingBy(Entry::getParentId));
        }

        // 3. 构建 storageId -> Storage 映射
        Map<Long, Storage> storageMap = buildStorageMap(roots, childrenMap);

        // 4. 遍历根节点，构建下载任务tre
        for (Entry root : roots) {
            String rootPath = root.getEntryName();
            if (root.getEntryType() == TYPE_FOLDER) {
                // 文件夹：递归遍历所有子节点，生成带路径的下载任务
                buildTasks(root, rootPath, childrenMap, storageMap, tasks);
            } else {
                // 文件：直接从 storageMap 获取 objectKey
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
     * 构建 storageId -> Storage 的映射
     * 收集所有文件（包括根节点和后代节点）的 storageId，批量查询后构建映射表
     */
    private Map<Long, Storage> buildStorageMap(List<Entry> roots, Map<Long, List<Entry>> childrenMap) {
        Set<Long> storageIds = new HashSet<>();

        // 收集根节点中的文件 storageId
        for (Entry root : roots) {
            if (root.getEntryType() == TYPE_FILE && root.getStorageId() != null && root.getStorageId() != 0) {
                storageIds.add(root.getStorageId());
            }
        }

        // 收集后代节点中的文件 storageId
        for (List<Entry> children : childrenMap.values()) {
            for (Entry child : children) {
                if (child.getEntryType() == TYPE_FILE && child.getStorageId() != null && child.getStorageId() != 0) {
                    storageIds.add(child.getStorageId());
                }
            }
        }

        if (storageIds.isEmpty()) {
            return new HashMap<>();
        }

        // 批量查询 Storage 记录，构建 storageId -> Storage 映射
        List<Storage> storages = storageMapper.selectList(
                Wrappers.<Storage>lambdaQuery()
                        .in(Storage::getId, storageIds));

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
        if (node.getEntryType() == TYPE_FOLDER) {
            tasks.add(DownloadTask.emptyDir(currentPath + "/"));

            List<Entry> children = childrenMap.getOrDefault(node.getId(), Collections.emptyList());
            for (Entry child : children) {
                String childPath = currentPath + "/" + child.getEntryName();
                if (child.getEntryType() == TYPE_FOLDER) {
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
     * 将单个任务写入 ZIP 流
     * - 空文件夹：直接写入 ZIPEntry
     * - 文件：流式读取 MinIO 对象并写入 ZIP
     */
    private void writeToZip(ZipOutputStream zos, DownloadTask task, Set<String> entryNames)
            throws Exception {
        String entryPath = getUniqueEntryName(task.zipPath, entryNames);
        entryNames.add(entryPath);

        ZipEntry entry = new ZipEntry(entryPath);
        if (task.isEmptyDir()) {
            zos.putNextEntry(entry);
            zos.closeEntry();
            return;
        }

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
     * 获取唯一的 ZIP 条目名称
     * 如果存在同名文件，自动重命名为 name(n).ext 格式
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
     * 用于封装单个文件或文件夹的下载信息
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