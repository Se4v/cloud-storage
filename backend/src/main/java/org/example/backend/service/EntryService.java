package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.entity.Entry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EntryService {
    private final EntryMapper entryMapper;
    private final StorageMapper storageMapper;

    public EntryService(EntryMapper entryMapper, StorageMapper storageMapper) {
        this.entryMapper = entryMapper;
        this.storageMapper = storageMapper;
    }

    /**
     * 永久删除条目
     * @param entryIds 要删除的条目ID列表
     */
    @Transactional
    public void permanentlyDeletedEntry(List<Long> entryIds) {
        int count = entryMapper.deleteByIds(entryIds);
        if (count != entryIds.size()) throw new BusinessException("删除失败");
    }

    /**
     * 更新条目状态
     * @param entries 要更新的条目列表
     */
    @Transactional
    public void updateEntryStatus(List<Entry> entries) {
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
            if (count != allFileIds.size()) throw new BusinessException("Permanently Delete files failed");
        }

        // 删除所有文件夹（更新状态为永久删除）
        List<Long> allFolderIds = new ArrayList<>(folderIds);
        allFolderIds.addAll(allChildFolderIds);
        if (!allFolderIds.isEmpty()) {
            int count = entryMapper.update(
                    Wrappers.<Entry>lambdaUpdate()
                            .set(Entry::getStatus, DbConsts.ENTRY_STATUS_PERMANENT_DELETED)
                            .in(Entry::getId, allFolderIds));
            if (count != allFolderIds.size()) throw new BusinessException("Permanently Delete folders failed");
        }

        // 物理文件的引用计数减1
        if (!storageIds.isEmpty()) {
            Map<Long, Integer> countMap = storageIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> 1, Integer::sum));
            int count = storageMapper.batchUpdateRefCount(countMap);
            if (count != countMap.size()) throw new BusinessException("Update storage ref count failed");
        }
    }
}
