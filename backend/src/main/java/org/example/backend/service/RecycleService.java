package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.args.DeleteEntryArgs;
import org.example.backend.model.args.RestoreEntryArgs;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.view.RecycleView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecycleService {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private DriveMapper driveMapper;

    private static final int FILE = 1;
    private static final int FOLDER = 2;
    private static final int UNDELETED = 1;
    private static final int DELETED = 2;
    private static final int PERMANENTLY_DELETED = 3;

    /**
     * 查询用户回收站中的条目
     */
    public List<RecycleView> listEntries(Long userId) {
        // 查询用户回收站中的条目（状态为已删除且未过期）
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getDeleterId, userId)
                .eq(Entry::getStatus, DELETED)
                .gt(Entry::getExpiredAt, LocalDateTime.now());
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) return List.of();

        // 获取关联的空间ID
        Set<Long> driveIds = entries.stream().map(Entry::getDriveId).collect(Collectors.toSet());

        // 批量查询空间信息
        Map<Long, Drive> driveMap;
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.in(Drive::getId, driveIds);
        List<Drive> drives = driveMapper.selectList(driveQuery);
        if (drives == null || drives.isEmpty()) throw new BusinessException("<UNK>");
        driveMap = drives.stream().collect(Collectors.toMap(Drive::getId, drive -> drive));

        // 组装结果
        List<RecycleView> recycleViews = entries.stream()
                .map(entry -> {
                    Drive drive = driveMap.get(entry.getDriveId());
                    String path = drive.getDriveType() == 1 ? "个人空间/项目文件" : "企业空间/" + drive.getDriveName();

                    return RecycleView.builder()
                            .id(entry.getId())
                            .name(entry.getEntryName())
                            .type(entry.getEntryType())
                            .path(path)
                            .size(entry.getFileSize())
                            .deleteTime(entry.getDeletedAt())
                            .expireTime(entry.getExpiredAt())
                            .build();
                })
                .toList();

        return recycleViews;
    }

    /**
     * 批量恢复条目
     */
    @Transactional
    public void restoreEntries(RestoreEntryArgs args) {
        // 查询要恢复的条目
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.in(Entry::getId, args.getIds()).eq(Entry::getStatus, DELETED);
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        // 分类为文件和文件夹
        Map<Integer, List<Long>> groupedMap = entries.stream()
                .collect(Collectors.groupingBy(
                        Entry::getEntryType, // 按EntryType分组
                        Collectors.mapping(Entry::getId, Collectors.toList()) // 映射为id列表
                ));
        List<Long> fileIds = groupedMap.getOrDefault(FILE, new ArrayList<>());
        List<Long> folderIds = groupedMap.getOrDefault(FOLDER, new ArrayList<>());

        // 恢复文件
        if (!fileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> fileUpdate = new LambdaUpdateWrapper<>();
            fileUpdate.set(Entry::getStatus, UNDELETED)
                    .set(Entry::getDeletedAt, null)
                    .set(Entry::getExpiredAt, null)
                    .set(Entry::getDeleterId, 0)
                    .in(Entry::getId, fileIds);
            int count = entryMapper.update(fileUpdate);
            if (count != fileIds.size()) throw new BusinessException("Restore files failed");
        }

        // 恢复文件夹及其所有子项
        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectDescendantsByFolderId(folderIds);
            List<Long> allFolderIds = new ArrayList<>(folderIds);
            allFolderIds.addAll(
                    children.stream()
                            .map(Entry::getId)
                            .toList()
            );

            LambdaUpdateWrapper<Entry> folderUpdate = new LambdaUpdateWrapper<>();
            folderUpdate.set(Entry::getStatus, UNDELETED)
                    .set(Entry::getDeletedAt, null)
                    .set(Entry::getExpiredAt, null)
                    .set(Entry::getDeleterId, 0)
                    .in(Entry::getId, allFolderIds);
            int count = entryMapper.update(folderUpdate);
            if (count != allFolderIds.size()) throw new BusinessException("Restore folders failed");
        }
    }

    /**
     * 批量永久删除条目
     */
    @Transactional
    public void deleteEntries(DeleteEntryArgs args) {
        // 查询要删除的条目
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.in(Entry::getId, args.getIds()).eq(Entry::getStatus, DELETED);
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        // 分类为文件和文件夹，同时收集所有文件的storageId
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();
        List<Long> storageIds = new ArrayList<>();
        entries.forEach(entry -> {
            if (entry.getEntryType() == FILE) {
                fileIds.add(entry.getId());
                storageIds.add(entry.getStorageId());
            } else {
                folderIds.add(entry.getId());
            }
        });

        // 查询文件夹的所有子项并分类
        List<Long> allChildFileIds = new ArrayList<>();
        List<Long> allChildFolderIds = new ArrayList<>();
        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectDescendantsByFolderId(folderIds);
            children.forEach(entry -> {
                if (entry.getEntryType() == FILE) {
                    allChildFileIds.add(entry.getId());
                    storageIds.add(entry.getStorageId());
                } else {
                    allChildFolderIds.add(entry.getId());
                }
            });
        }

        // 删除所有文件（更新状态为永久删除）
        List<Long> allFileIds = new ArrayList<>(fileIds);
        allFileIds.addAll(allChildFileIds);
        if (!allFileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> fileUpdate = new LambdaUpdateWrapper<>();
            fileUpdate.set(Entry::getStatus, PERMANENTLY_DELETED).in(Entry::getId, allFileIds);
            int count = entryMapper.update(fileUpdate);
            if (count != allFileIds.size()) throw new BusinessException("Permanently Delete files failed");
        }

        // 删除所有文件夹（更新状态为永久删除）
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> folderUpdate = new LambdaUpdateWrapper<>();
            folderUpdate.set(Entry::getStatus, PERMANENTLY_DELETED).in(Entry::getId, allFolderIds);
            int count = entryMapper.update(folderUpdate);
            if (count != allFolderIds.size()) throw new BusinessException("Permanently Delete folders failed");
        }

        // 物理文件的引用计数减1
        if (!storageIds.isEmpty()) {
            Map<Long, Integer> countMap = storageIds.stream().collect(Collectors.toMap(
                    id -> id, id -> 1, Integer::sum));
            int count = storageMapper.batchUpdateRefCount(countMap);
            if (count != countMap.size()) throw new BusinessException("Update storage ref count failed");
        }
    }

    /**
     * 清空用户回收站
     */
    @Transactional
    public void clearEntries(Long userId) {
        // 查询用户回收站中所有条目
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getDeleterId, userId).eq(Entry::getStatus, DELETED);
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) throw new BusinessException("clear recycle bin failed");

        // 2. 分类为文件和文件夹，同时收集所有文件的storageId
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();
        List<Long> storageIds = new ArrayList<>();
        entries.forEach(entry -> {
            if (entry.getEntryType() == FILE) {
                fileIds.add(entry.getId());
                storageIds.add(entry.getStorageId());
            } else {
                folderIds.add(entry.getId());
            }
        });

        // 查询文件夹的所有子项并分类
        List<Long> allChildFileIds = new ArrayList<>();
        List<Long> allChildFolderIds = new ArrayList<>();
        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectDescendantsByFolderId(folderIds);
            children.forEach(entry -> {
                if (entry.getEntryType() == FILE) {
                    allChildFileIds.add(entry.getId());
                    storageIds.add(entry.getStorageId());
                } else {
                    allChildFolderIds.add(entry.getId());
                }
            });
        }

        // 删除所有文件
        List<Long> allFileIds = new ArrayList<>(fileIds);
        allFileIds.addAll(allChildFileIds);
        if (!allFileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> fileUpdate = new LambdaUpdateWrapper<>();
            fileUpdate.set(Entry::getStatus, PERMANENTLY_DELETED).in(Entry::getId, allFileIds);
            int count = entryMapper.update(fileUpdate);
            if (count != allFileIds.size()) throw new BusinessException("clear recycle bin failed");
        }

        // 删除所有文件夹
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> folderUpdate = new LambdaUpdateWrapper<>();
            folderUpdate.set(Entry::getStatus, PERMANENTLY_DELETED).in(Entry::getId, allFolderIds);
            int count = entryMapper.update(folderUpdate);
            if (count != allFolderIds.size()) throw new BusinessException("clear recycle bin failed");
        }

        // 物理文件的引用计数减1
        if (!storageIds.isEmpty()) {
            Map<Long, Integer> countMap = storageIds.stream().collect(Collectors.toMap(
                    id -> id, id -> 1, Integer::sum));
            int count = storageMapper.batchUpdateRefCount(countMap);
            if (count != countMap.size()) throw new BusinessException("Update storage ref count failed");
        }
    }
}
