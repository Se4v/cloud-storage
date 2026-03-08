package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.result.RecycleDetailResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecycleService {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private StorageMapper storageMapper;

    private static final int ENTRY_TYPE_FILE = 1;
    private static final int ENTRY_TYPE_FOLDER = 2;
    private static final int STATUS_UNDELETED = 1;
    private static final int STATUS_DELETED = 2;
    private static final int STATUS_PERMANENTLY_DELETED = 3;

    /**
     * 查询用户回收站中的条目
     */
    public List<RecycleDetailResult> listEntries(Long userId) {
        List<RecycleDetailResult> results = entryMapper.selectValidRecycleBinEntryByUserId(userId);
        if (results == null) {
            return List.of();
        }
        return results;
    }

    /**
     * 批量恢复条目
     */
    @Transactional
    public void restoreEntries(List<Long> ids) {
        // 1. 查询要恢复的条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Entry::getId, ids)
                .eq(Entry::getStatus, STATUS_DELETED);
        List<Entry> entries = entryMapper.selectList(queryWrapper);
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        // 2. 分类为文件和文件夹
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();

        for (Entry entry : entries) {
            if (entry.getEntryType() == ENTRY_TYPE_FILE) {
                fileIds.add(entry.getId());
            } else if (entry.getEntryType() == ENTRY_TYPE_FOLDER) {
                folderIds.add(entry.getId());
            }
        }

        // 3. 恢复文件
        if (!fileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Entry::getStatus, STATUS_UNDELETED)
                    .in(Entry::getId, fileIds);
            int count = entryMapper.update(updateWrapper);
            if (count != fileIds.size()) throw new BusinessException("Restore files failed");
        }

        // 4. 恢复文件夹及其所有子项
        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectRecursiveChildEntryIdsBatch(folderIds);
            List<Long> allFolderIds = new ArrayList<>(folderIds);
            for (Entry child : children) {
                if (child != null && child.getId() != null) {
                    allFolderIds.add(child.getId());
                }
            }

            LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Entry::getStatus, STATUS_UNDELETED)
                    .in(Entry::getId, allFolderIds);
            int count = entryMapper.update(updateWrapper);
            if (count != allFolderIds.size()) throw new BusinessException("Restore folders failed");
        }
    }

    /**
     * 批量永久删除条目
     */
    @Transactional
    public void permanentlyDeleteEntries(List<Long> ids) {
        // 1. 查询要删除的条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Entry::getId, ids)
                .eq(Entry::getStatus, STATUS_DELETED);
        List<Entry> entries = entryMapper.selectList(queryWrapper);
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        // 2. 分类为文件和文件夹，同时收集所有文件的storageId
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();
        Set<Long> fileStorageIds = new HashSet<>();

        for (Entry entry : entries) {
            if (entry.getEntryType() == ENTRY_TYPE_FILE) {
                fileIds.add(entry.getId());
                fileStorageIds.add(entry.getStorageId());
            } else if (entry.getEntryType() == ENTRY_TYPE_FOLDER) {
                folderIds.add(entry.getId());
            }
        }

        // 3. 查询文件夹的所有子项并分类
        List<Long> allChildFileIds = new ArrayList<>();
        List<Long> allChildFolderIds = new ArrayList<>();

        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectRecursiveChildEntryIdsBatch(folderIds);
            for (Entry child : children) {
                if (child.getEntryType() == ENTRY_TYPE_FILE) {
                    allChildFileIds.add(child.getId());
                    fileStorageIds.add(child.getStorageId());
                } else if (child.getEntryType() == ENTRY_TYPE_FOLDER) {
                    allChildFolderIds.add(child.getId());
                }
            }
        }

        // 4. 删除所有文件（更新状态为永久删除）
        List<Long> allFileIds = new ArrayList<>(fileIds);
        allFileIds.addAll(allChildFileIds);
        if (!allFileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Entry::getStatus, STATUS_PERMANENTLY_DELETED)
                    .in(Entry::getId, allFileIds);
            int count = entryMapper.update(updateWrapper);
            if (count != allFileIds.size()) throw new BusinessException("Permanently Delete files failed");
        }

        // 5. 删除所有文件夹（更新状态为永久删除）
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Entry::getStatus, STATUS_PERMANENTLY_DELETED)
                    .in(Entry::getId, allFolderIds);
            int count = entryMapper.update(updateWrapper);
            if (count != allFolderIds.size()) throw new BusinessException("Permanently Delete folders failed");
        }

        // 6. 所有文件的storageId对应的refCount减1
        if (!fileStorageIds.isEmpty()) {
            List<Long> storageIdList = new ArrayList<>(fileStorageIds);
            LambdaUpdateWrapper<Storage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.setDecrBy(Storage::getRefCount, 1)
                    .in(Storage::getId, fileStorageIds);
            int count = storageMapper.update(updateWrapper);
            if (count != storageIdList.size()) throw new BusinessException("Update storage ref count failed");
        }
    }

    /**
     * 清空用户回收站
     */
    @Transactional
    public void clearEntries(Long userId) {
        // 1. 查询用户回收站中所有条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entry::getDeleterId, userId)
                .eq(Entry::getStatus, STATUS_DELETED);
        List<Entry> entries = entryMapper.selectList(queryWrapper);
        if (entries == null || entries.isEmpty()) throw new BusinessException("clear recycle bin failed");

        // 2. 分类为文件和文件夹，同时收集所有文件的storageId
        List<Long> fileIds = new ArrayList<>();
        List<Long> folderIds = new ArrayList<>();
        Set<Long> fileStorageIds = new HashSet<>();

        for (Entry entry : entries) {
            if (entry.getEntryType() == ENTRY_TYPE_FILE) {
                fileIds.add(entry.getId());
                fileStorageIds.add(entry.getStorageId());
            } else if (entry.getEntryType() == ENTRY_TYPE_FOLDER) {
                folderIds.add(entry.getId());
            }
        }

        // 3. 查询文件夹的所有子项并分类
        List<Long> allChildFileIds = new ArrayList<>();
        List<Long> allChildFolderIds = new ArrayList<>();

        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectRecursiveChildEntryIdsBatch(folderIds);
            for (Entry child : children) {
                if (child.getEntryType() == ENTRY_TYPE_FILE) {
                    allChildFileIds.add(child.getId());
                    fileStorageIds.add(child.getStorageId());
                } else if (child.getEntryType() == ENTRY_TYPE_FOLDER) {
                    allChildFolderIds.add(child.getId());
                }
            }
        }

        // 4. 删除所有文件（更新状态为永久删除）
        List<Long> allFileIds = new ArrayList<>(fileIds);
        allFileIds.addAll(allChildFileIds);
        if (!allFileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Entry::getStatus, STATUS_PERMANENTLY_DELETED)
                    .in(Entry::getId, allFileIds);
            int count = entryMapper.update(updateWrapper);
            if (count != allFileIds.size()) throw new BusinessException("clear recycle bin failed");
        }

        // 5. 删除所有文件夹（更新状态为永久删除）
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Entry::getStatus, STATUS_PERMANENTLY_DELETED)
                    .in(Entry::getId, allFolderIds);
            int count = entryMapper.update(updateWrapper);
            if (count != allFolderIds.size()) throw new BusinessException("clear recycle bin failed");
        }

        // 6. 所有文件的storageId对应的refCount减1
        if (!fileStorageIds.isEmpty()) {
            List<Long> storageIdList = new ArrayList<>(fileStorageIds);
            LambdaUpdateWrapper<Storage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.setDecrBy(Storage::getRefCount, 1)
                    .in(Storage::getId, fileStorageIds);
            int count = storageMapper.update(updateWrapper);
            if (count != storageIdList.size()) throw new BusinessException("Update storage ref count failed");
        }
    }
}
