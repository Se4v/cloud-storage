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

    public Storage findBlobBySha256(String sha256) {
        return storageMapper.selectBySha256(sha256);
    }

    @Transactional
    public void saveEntryWithIncreaseRefCount(Entry entry, String sha256) {
        int entryCount = entryMapper.insert(entry);
        if (entryCount != 1) {
            throw new BusinessException("Entry插入失败，受影响行数：" + entryCount);
        }

        int blobCount = storageMapper.increaseRefCountBySha256(sha256);
        if (blobCount != 1) {
            throw new BusinessException("Blob<UNK>" + blobCount);
        }
    }

    @Transactional
    public void saveEntryAndBlobWithUpdateDriveQuota(Entry entry, Storage storage) {
        int blobCount = storageMapper.insert(storage);
        if (blobCount != 1) {
            throw new BusinessException("Blob插入失败，受影响行数：" + blobCount);
        }

        Long storageId = storage.getId();
        if (storageId == null) {
            throw new BusinessException("Blob插入后主键未回填");
        }

        entry.setStorageId(storageId);
        int entryCount = entryMapper.insert(entry);
        if (entryCount != 1) {
            throw new BusinessException("Entry插入失败，受影响行数：" + entryCount);
        }

        int driveCount = driveMapper.increaseUsedQuotaById(entry.getDriveId(), entry.getFileSize());
        if (driveCount != 1) {
            throw new BusinessException("空间配额更新失败，driveId："+ entry.getDriveId() + "，受影响行数：" + driveCount);
        }
    }
}
