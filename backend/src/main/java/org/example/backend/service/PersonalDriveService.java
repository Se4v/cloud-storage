package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.model.args.CreateFolderArgs;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.view.EntryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonalDriveService {
    @Autowired
    private EntryMapper entryMapper;

    @Transactional
    public void createFolder(CreateFolderArgs args, Long userId) {
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

    public List<EntryView> getEntries(Long userId, Long parentId, Long driveId) {

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

    public void searchEntry(String targetName) {
        // 文件名校验
        String invalidChars = "[\\\\/:*?\"<>|]";
        if (targetName.length() > 100 || targetName.matches(".*" + invalidChars + ".*")) {
            throw new BusinessException("New entry name invalid");
        }


    }

    @Transactional
    public void deleteEntries() {

    }
}
