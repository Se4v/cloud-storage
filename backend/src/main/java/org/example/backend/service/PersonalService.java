package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.model.args.CreateFolderArgs;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PersonalService {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DriveMapper driveMapper;

    public List<Entry> listEntries(Long userId, Long parentId) {
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getUserId, userId)
                .eq(Drive::getDriveType, 1);
        Drive drive = driveMapper.selectOne(driveQuery);
        if (drive == null) throw new BusinessException("<UNK>");

        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        if (parentId == null) parentId = 0L;
        entryQuery.eq(Entry::getParentId, parentId)
                .eq(Entry::getDriveId, drive.getId())
                .eq(Entry::getStatus, 1);
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) throw new BusinessException("<UNK>");

        return entries;
    }

    @Transactional
    public void createFolder(CreateFolderArgs args, Long userId) {
        // 目标父目录下是否存在同名条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entry::getParentId, args.getParentId())
                .eq(Entry::getEntryName, args.getFolderName());
        Entry sameEntry = entryMapper.selectOne(queryWrapper);
        if (sameEntry != null) throw new BusinessException("Folder already exists");

        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getUserId, userId)
                .eq(Drive::getDriveType, 1);
        Drive drive = driveMapper.selectOne(driveQuery);

        // 创建记录
        int count = entryMapper.insert(Entry.builder()
                .driveId(drive.getId())
                .userId(userId)
                .parentId(args.getParentId())
                .storageId(0L)
                .entryName(args.getFolderName())
                .entryType(2)
                .status(1)
                .build());
        if (count != 1) throw new BusinessException("Create folder failed");
    }

    @Transactional
    public void moveEntries(List<Long> entryIds, Long targetId, Long userId) {
        Entry entry = entryMapper.selectById(targetId);
        if (entry == null) throw new BusinessException("Entry does not exist");

        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Entry::getId, entryIds)
                .set(Entry::getParentId, targetId)
                .set(Entry::getUpdaterId, userId);

        int count = entryMapper.update(updateWrapper);
        if (count != entryIds.size()) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void renameEntry(Long entryId, String newEntryName) {
        Entry entry = entryMapper.selectById(entryId);
        if (entry == null) throw new BusinessException("Entry does not exist");

        // 新文件名校验
        if (!validateFileName(newEntryName)) throw new BusinessException("Invalid file name");

        // 重命名
        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Entry::getEntryName, newEntryName)
                .eq(Entry::getId, entryId);
        int count = entryMapper.update(updateWrapper);
        if (count != 1) throw new BusinessException("Rename entry failed");
    }

    public List<Entry> searchEntry(String targetName, Long userId) {
        // 文件名校验
        if (!validateFileName(targetName)) throw new BusinessException("Invalid file name");

        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Entry::getEntryName, targetName);
        List<Entry> entries = entryMapper.selectList(queryWrapper);

        if (entries == null || entries.isEmpty()) throw new BusinessException("Search entry failed");

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
        int fileCount = entryMapper.updateStatusBatch(files, 2);
        if (fileCount != files.size()) {
            throw new BusinessException("Delete files failed");
        }

        // 删除文件夹
        List<Long> children = entryMapper.selectRecursiveChildEntryIdsBatch(folders);
        Set<Long> allIds = new HashSet<>(folders);
        allIds.addAll(children);
        int folderCount = entryMapper.updateStatusBatch(new ArrayList<>(allIds), 2);
        if (folderCount != allIds.size()) {
            throw new BusinessException("Delete folders failed");
        }
    }

    private boolean validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.length() > 100) return false;

        // 检查保留字（CON, PRN, AUX, NUL, COM1-9, LPT1-9）
        String upper = fileName.toUpperCase();
        if (upper.matches("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$")) return false;

        // 检查非法字符 \ / : * ? " < > | 以及首尾空格/点
        return !fileName.matches(".*[\\\\/:*?\"<>|].*") &&
                !fileName.startsWith(" ") && !fileName.endsWith(" ") &&
                !fileName.endsWith(".");
    }
}
