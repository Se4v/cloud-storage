package org.example.backend.service;

import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.model.result.RecycleDetailResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecycleBinService {
    @Autowired
    private EntryMapper entryMapper;

    private static final int ENTRY_TYPE_FILE = 1;
    private static final int ENTRY_TYPE_FOLDER = 2;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_PERMANENTLY_DELETED = 3;

    public List<RecycleDetailResult> getEntries(Long userId) {
        // 查询未过期也未永久删除的文件
        List<RecycleDetailResult> results = entryMapper.selectValidRecycleBinEntryByUserId(userId);
        return results == null ? List.of() : results;
    }

    @Transactional
    public void restoreEntries(List<Long> ids) {
        batchProcessEntries(ids, STATUS_NORMAL, "Restore");
    }

    @Transactional
    public void permanentlyDeleteEntries(List<Long> ids) {
        batchProcessEntries(ids, STATUS_PERMANENTLY_DELETED, "Permanently Delete");
    }

    @Transactional
    public void clear(Long userId) {
        List<RecycleDetailResult> results = entryMapper.selectValidRecycleBinEntryByUserId(userId);
        if (results == null || results.isEmpty()) {
            throw new BusinessException("clear recycle bin failed");
        }

        List<Long> allIds = results.stream()
                .map(RecycleDetailResult::getEntryId)
                .filter(Objects::nonNull)
                .toList();

        permanentlyDeleteEntries(allIds);
    }

    /**
     * 通用处理入口：恢复或永久删除
     */
    private void batchProcessEntries(List<Long> ids, int targetStatus, String operationName) {
        List<RecycleDetailResult> results = entryMapper.selectRecycleBinEntryByEntryIds(ids);
        if (results == null || results.isEmpty()) {
            throw new BusinessException("<UNK>");
        }

        // 分类
        Map<Integer, List<Long>> grouped = results.stream()
                .filter(r -> r != null && r.getEntryId() != null && r.getEntryType() != null)
                .collect(Collectors.groupingBy(
                        RecycleDetailResult::getEntryType,
                        Collectors.mapping(RecycleDetailResult::getEntryId, Collectors.toList())
                ));
        EntryGroups groups = new EntryGroups(
                grouped.getOrDefault(ENTRY_TYPE_FILE, List.of()),
                grouped.getOrDefault(ENTRY_TYPE_FOLDER, List.of())
        );

        // 处理文件
        if (!groups.files.isEmpty()) {
            int count = entryMapper.updateStatusBatch(groups.files, targetStatus);
            if (count != groups.files.size()) {
                throw new BusinessException(operationName + " files failed");
            }
        }

        // 处理文件夹及其子项
        if (!groups.folders.isEmpty()) {
            List<Long> children = entryMapper.selectRecursiveChildEntryIdsBatch(groups.folders);
            Set<Long> allIds = new HashSet<>(groups.folders);
            allIds.addAll(children);
            int count = entryMapper.updateStatusBatch(new ArrayList<>(allIds), targetStatus);
            if (count != allIds.size()) {
                throw new BusinessException(operationName + " folders failed");
            }
        }
    }

    /**
     * 分组结果记录类
     */
    private record EntryGroups(List<Long> files, List<Long> folders) {}
}
