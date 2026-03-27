package org.example.backend.repository;

import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.result.EntryWithBlobResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class FileRepository {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private DriveMapper driveMapper;

    public List<EntryWithBlobResult> findDescendantsWithBlob(Long entryId) {
        return entryMapper.selectDescendantsWithBlobByEntryIds(entryId);
    }

    public List<EntryWithBlobResult> findEntriesWithBlob(List<Long> entryIds) {
        return entryMapper.selectBatchWithBlobByEntryIds(entryIds);
    }

    @Transactional
    public void saveEntryAndBlobWithUpdateDriveQuota(Entry entry, Storage storage) {

    }
}
