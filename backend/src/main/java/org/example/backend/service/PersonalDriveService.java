package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.model.args.CreateFolderArgs;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PersonalDriveService {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DriveMapper driveMapper;

    public List<Entry> listEntries(Long userId, Long parentId) {
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getUserId, userId);
        Drive existDrive = driveMapper.selectOne(driveQuery);
        if (existDrive == null) {
            throw new BusinessException("<UNK>");
        }

        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getParentId, parentId)
                .eq(Entry::getDriveId, existDrive.getId());
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) {
            throw new BusinessException("<UNK>");
        }

        return entries;
    }

    @Transactional
    public void createEntry(CreateFolderArgs args, Long userId) {
        // 目标父目录下是否存在同名条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entry::getParentId, args.getParentId())
                .eq(Entry::getEntryName, args.getFolderName());
        Entry sameEntry = entryMapper.selectOne(queryWrapper);
        if (sameEntry != null) {
            throw new BusinessException("Folder already exists");
        }

        // 创建记录
        int count = entryMapper.insert(Entry.builder()
                .driveId(args.getDriveId())
                .userId(userId)
                .parentId(args.getParentId())
                .storageId(0L)
                .entryName(args.getFolderName())
                .entryType(2)
                .build());
        if (count != 1) {
            throw new BusinessException("Create folder failed");
        }
    }

    @Transactional
    public void moveEntries(List<Long> entryIds, Long targetParentId, Long userId) {
        Entry existEntry = entryMapper.selectById(targetParentId);
        if (existEntry == null) {
            throw new BusinessException("Entry does not exist");
        }

        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Entry::getId, entryIds)
                .set(Entry::getParentId, targetParentId)
                .set(Entry::getUpdaterId, userId);

        int count = entryMapper.update(updateWrapper);
        if (count != entryIds.size()) {
            throw new BusinessException("Move entry failed");
        }
    }

    @Transactional
    public void renameEntry(Long entryId, String newEntryName) {
        Entry existEntry = entryMapper.selectById(entryId);
        if (existEntry == null) {
            throw new BusinessException("Entry does not exist");
        }

        // 新文件名校验
        String invalidChars = "[\\\\/:*?\"<>|]";
        if (newEntryName.length() > 100 || newEntryName.matches(".*" + invalidChars + ".*")) {
            throw new BusinessException("New entry name invalid");
        }

        // 重命名
        UpdateWrapper<Entry> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("entry_id", entryId)
                .set("entry_name", newEntryName);
        int count = entryMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("Rename entry failed");
        }
    }

    public List<Entry> searchEntry(String targetName, Long userId) {
        // 文件名校验
        String invalidChars = "[\\\\/:*?\"<>|]";
        if (targetName.length() > 100 || targetName.matches(".*" + invalidChars + ".*")) {
            throw new BusinessException("New entry name invalid");
        }

        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Entry::getEntryName, targetName);
        List<Entry> entries = entryMapper.selectList(queryWrapper);

        if (entries == null || entries.isEmpty()) {
            throw new BusinessException("Search entry failed");
        }

        return entries;
    }

    @Transactional
    public void deleteEntries(List<Long> entryIds) {
        // 获取数据
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Entry::getId, entryIds);
        List<Entry> entries = entryMapper.selectList(queryWrapper);
        if (entries == null || entries.isEmpty()) {
            throw new BusinessException("entry does not exist");
        }

        // 文件分类
        List<Long> files = entries.stream()
                .filter(entry -> entry.getEntryType() != null && entry.getEntryType() == 1)
                .map(Entry::getId)
                .filter(Objects::nonNull)
                .toList();
        List<Long> folders = entries.stream()
                .filter(entry -> entry.getEntryType() != null && entry.getEntryType() == 2)
                .map(Entry::getId)
                .filter(Objects::nonNull)
                .toList();

        // 删除文件
        int deleteFileCount = entryMapper.updateDeleteFlagBatch(files);
        if (deleteFileCount != files.size()) {
            throw new BusinessException("Delete files failed");
        }

        // 删除文件夹
        List<Long> children = entryMapper.selectRecursiveChildEntryIdsBatch(folders);
        int deleteFolderCount = entryMapper.updateDeleteFlagBatch(children);
        if (deleteFolderCount != children.size()) {
            throw new BusinessException("Delete folders failed");
        }
    }

    private boolean validateFileName() {
        return false;
    }
}
