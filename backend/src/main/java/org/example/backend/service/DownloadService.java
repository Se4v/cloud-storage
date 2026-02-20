package org.example.backend.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.config.MinioConfig;
import org.example.backend.model.result.EntryWithBlobResult;
import org.example.backend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class DownloadService {
    @Autowired
    private MinioConfig minioConfig;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private FileRepository fileRepository;

    private static final int TYPE_FILE = 1;
    private static final int TYPE_FOLDER = 2;

    public StreamingResponseBody download(List<Long> entryIds) {
        return outputStream -> {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
                Set<String> entryNames = new HashSet<>();

                List<DownloadTask> tasks = collectDownloadTasks(entryIds);

                for (DownloadTask task : tasks) {
                    writeToZip(zipOutputStream, task, entryNames);
                }

                zipOutputStream.finish();
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new BusinessException("");
            }
        };
    }

    private List<DownloadTask> collectDownloadTasks(List<Long> rootIds) {
        List<DownloadTask> tasks = new ArrayList<>();

        // 批量查询根节点（1次查询）
        List<EntryWithBlobResult> roots = fileRepository.findEntriesWithBlob(rootIds);
        Map<Long, EntryWithBlobResult> rootMap = roots.stream()
                .collect(Collectors.toMap(EntryWithBlobResult::getEntryId, f -> f));

        // 处理每个根节点
        for (Long rootId : rootIds) {
            EntryWithBlobResult root = rootMap.get(rootId);
            if (root == null) continue;

            if (root.getEntryType() == TYPE_FOLDER) {
                // 一次性查询该文件夹下所有后代（1次查询/文件夹）
                List<EntryWithBlobResult> descendants = fileRepository.findDescendantsWithBlob(rootId);

                // 构建 parentId -> children 映射（内存中组装树）
                Map<Long, List<EntryWithBlobResult>> childrenMap = descendants.stream()
                        .collect(Collectors.groupingBy(EntryWithBlobResult::getParentId));

                // 深度优先遍历，生成带路径的下载任务
                buildTasks(root, root.getEntryName(), childrenMap, tasks);
            } else {
                // 单个文件直接添加
                tasks.add(new DownloadTask(root, root.getEntryName(), false));
            }
        }
        return tasks;
    }

    private void buildTasks(EntryWithBlobResult node, String currentPath,
                               Map<Long, List<EntryWithBlobResult>> childrenMap,
                               List<DownloadTask> tasks) {
        if (node.getEntryType() == TYPE_FOLDER) {
            // 添加空目录条目（保留文件夹结构）
            tasks.add(DownloadTask.emptyDir(currentPath + "/"));

            // 递归处理子节点
            List<EntryWithBlobResult> children = childrenMap.getOrDefault(node.getEntryId(), Collections.emptyList());
            for (EntryWithBlobResult child : children) {
                buildTasks(child, currentPath + "/" + child.getEntryName(), childrenMap, tasks);
            }
        } else {
            tasks.add(new DownloadTask(node, currentPath, false));
        }
    }

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

        // 流式读取 MinIO 并写入 ZIP
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(task.result.getObjectKey())
                        .build())) {
            is.transferTo(zos); // Java 9+ 简洁写法，等价于 8KB 循环 buffer
        }
        zos.closeEntry();
    }

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

    @AllArgsConstructor
    private static class DownloadTask {
        EntryWithBlobResult result;
        String zipPath; // 在ZIP中的完整路径，如 "folderA/sub/1.txt"
        boolean emptyDir;

        static DownloadTask emptyDir(String path) {
            return new DownloadTask(null, path, true);
        }

        boolean isEmptyDir() { return emptyDir; }
    }

}
