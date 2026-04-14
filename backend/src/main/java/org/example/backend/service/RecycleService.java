package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.request.file.EntryDeletionReq;
import org.example.backend.model.request.file.EntryRestoreReq;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.response.file.RecycleResp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecycleService {
    private final EntryMapper entryMapper;
    private final StorageMapper storageMapper;
    private final DriveMapper driveMapper;

    public RecycleService(EntryMapper entryMapper, StorageMapper storageMapper, DriveMapper driveMapper) {
        this.entryMapper = entryMapper;
        this.storageMapper = storageMapper;
        this.driveMapper = driveMapper;
    }

    /**
     * 查询用户回收站中的条目
     * @return 回收站条目列表
     */
    public List<RecycleResp> listEntries() {
        // 查询用户回收站中的条目（状态为已删除且未过期）
        Long currentUserId = SecurityUtils.getUserId();
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDeleterId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_DELETED)
                        .gt(Entry::getExpiredAt, LocalDateTime.now()));
        if (entries == null || entries.isEmpty()) return List.of();

        // 获取关联的空间ID
        Set<Long> driveIds = entries.stream()
                .map(Entry::getDriveId)
                .collect(Collectors.toSet());

        // 批量查询空间信息
        Map<Long, Drive> driveMap;
        List<Drive> drives = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery().in(Drive::getId, driveIds));
        if (drives == null || drives.isEmpty()) return List.of();
        driveMap = drives.stream()
                .collect(Collectors.toMap(Drive::getId, drive -> drive));

        // 组装结果
        List<RecycleResp> resp = entries.stream()
                .map(entry -> {
                    Drive drive = driveMap.get(entry.getDriveId());
                    String path = drive.getDriveType() == DbConsts.DRIVE_TYPE_PERSONAL ?
                            "个人空间/项目文件" : "企业空间/" + drive.getDriveName();

                    return RecycleResp.builder()
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

        return resp;
    }

    /**
     * 批量恢复条目
     * @param req 条目恢复请求
     */
    @Transactional
    public void restoreEntries(EntryRestoreReq req) {
        // 查询要恢复的条目
        Long currentUserId = SecurityUtils.getUserId();
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDeleterId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_DELETED)
                        .in(Entry::getId, req.getIds()));
        if (entries == null || entries.isEmpty()) throw new BusinessException("文件不存在");

        // 分类为文件和文件夹
        Map<Integer, List<Long>> groupedMap = entries.stream()
                .collect(Collectors.groupingBy(
                        Entry::getEntryType, // 按EntryType分组
                        Collectors.mapping(Entry::getId, Collectors.toList()) // 映射为id列表
                ));
        List<Long> fileIds = groupedMap.getOrDefault(DbConsts.ENTRY_TYPE_FILE, new ArrayList<>());
        List<Long> folderIds = groupedMap.getOrDefault(DbConsts.ENTRY_TYPE_FOLDER, new ArrayList<>());

        // 恢复文件
        if (!fileIds.isEmpty()) {
            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED)
                            .set(Entry::getDeletedAt, null)
                            .set(Entry::getExpiredAt, null)
                            .set(Entry::getDeleterId, 0)
                            .in(Entry::getId, fileIds));
            if (count != fileIds.size()) throw new BusinessException("文件恢复失败");
        }

        // 恢复文件夹及其所有子项
        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectDescendantsByFolderId(folderIds);
            List<Long> allFolderIds = new ArrayList<>(folderIds);
            allFolderIds.addAll(children.stream().map(Entry::getId).toList());

            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED)
                            .set(Entry::getDeletedAt, null)
                            .set(Entry::getExpiredAt, null)
                            .set(Entry::getDeleterId, 0)
                            .in(Entry::getId, allFolderIds));
            if (count != allFolderIds.size()) throw new BusinessException("文件恢复失败");
        }
    }

    /**
     * 批量永久删除条目
     * @param req 条目删除请求
     */
    @Transactional
    public void deleteEntries(EntryDeletionReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        // 查询要删除的条目
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDeleterId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_DELETED)
                        .in(Entry::getId, req.getIds()));
        if (entries == null || entries.isEmpty()) throw new BusinessException("文件不存在");

        // 分类为文件和文件夹，同时收集所有文件的storageId
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();
        List<Long> storageIds = new ArrayList<>();
        entries.forEach(entry -> {
            if (entry.getEntryType() == DbConsts.ENTRY_TYPE_FILE) {
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
                if (entry.getEntryType() == DbConsts.ENTRY_TYPE_FILE) {
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
            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_PERMANENT_DELETED)
                            .in(Entry::getId, allFileIds));
            if (count != allFileIds.size()) throw new BusinessException("彻底删除文件失败");
        }

        // 删除所有文件夹（更新状态为永久删除）
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_PERMANENT_DELETED)
                            .in(Entry::getId, allFolderIds));
            if (count != allFolderIds.size()) throw new BusinessException("彻底删除文件失败");
        }

        // 物理文件的引用计数减1
        if (!storageIds.isEmpty()) {
            Map<Long, Integer> countMap = storageIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> 1, Integer::sum));
            int count = storageMapper.batchUpdateRefCount(countMap);
            if (count != countMap.size()) throw new BusinessException("彻底删除文件失败");
        }
    }

    /**
     * 清空用户回收站
     */
    @Transactional
    public void clearEntries() {
        // 查询用户回收站中所有条目
        Long currentUserId = SecurityUtils.getUserId();
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDeleterId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_DELETED));
        if (entries == null || entries.isEmpty()) throw new BusinessException("文件不存在");

        // 2. 分类为文件和文件夹，同时收集所有文件的storageId
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();
        List<Long> storageIds = new ArrayList<>();
        entries.forEach(entry -> {
            if (entry.getEntryType() == DbConsts.ENTRY_TYPE_FILE) {
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
                if (entry.getEntryType() == DbConsts.ENTRY_TYPE_FILE) {
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
            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_PERMANENT_DELETED)
                            .in(Entry::getId, allFileIds));
            if (count != allFileIds.size()) throw new BusinessException("清空回收站失败");
        }

        // 删除所有文件夹
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_PERMANENT_DELETED)
                            .in(Entry::getId, allFolderIds));
            if (count != allFolderIds.size()) throw new BusinessException("清空回收站失败");
        }

        // 物理文件的引用计数减1
        if (!storageIds.isEmpty()) {
            Map<Long, Integer> countMap = storageIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> 1, Integer::sum));
            int count = storageMapper.batchUpdateRefCount(countMap);
            if (count != countMap.size()) throw new BusinessException("清空回收站失败");
        }
    }
}
