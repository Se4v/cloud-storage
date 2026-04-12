package org.example.backend.service;

import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntryService {
    private final EntryMapper entryMapper;

    public EntryService(EntryMapper entryMapper) {
        this.entryMapper = entryMapper;
    }

    @Transactional
    public void permanentlyDeletedEntry(List<Long> entryIds) {
        int count = entryMapper.deleteByIds(entryIds);
        if (count != entryIds.size()) throw new BusinessException("删除失败");
    }
}
