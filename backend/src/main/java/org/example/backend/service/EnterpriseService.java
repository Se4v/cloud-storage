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

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnterpriseService {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DriveMapper driveMapper;

    private static final int ENTERPRISE_DRIVE = 2;
    private static final int UNDELETED = 1;
    private static final int DELETED = 2;
    private static final int FOLDER = 2;
    private static final int EXPIRE_DAYS = 15;

    public List<Entry> listEntries(Long nodeId, Long parentId) {
        // 查询空间
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getNodeId, nodeId)
                .eq(Drive::getDriveType, ENTERPRISE_DRIVE);
        Drive drive = driveMapper.selectOne(driveQuery);
        if (drive == null) throw new BusinessException("<UNK>");

        // 获取文件列表
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getParentId, parentId)
                .eq(Entry::getDriveId, drive.getId())
                .eq(Entry::getStatus, UNDELETED);
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
        driveQuery.eq(Drive::getId, args.getDriveId());
        Drive drive = driveMapper.selectOne(driveQuery);

        // 创建记录
        int count = entryMapper.insert(Entry.builder()
                .driveId(drive.getId())
                .userId(userId)
                .parentId(args.getParentId())
                .storageId(0L)
                .entryName(args.getFolderName())
                .entryType(FOLDER)
                .status(UNDELETED)
                .build());
        if (count != 1) throw new BusinessException("Create folder failed");
    }

    @Transactional
    public void moveEntries(List<Long> entryIds, Long targetId, Long userId) {
        Entry entry = entryMapper.selectById(targetId);
        if (entry == null) throw new BusinessException("Entry does not exist");

        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Entry::getId, entryIds)
                .set(Entry::getParentId, targetId);

        int count = entryMapper.update(updateWrapper);
        if (count != entryIds.size()) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void copyEntries(Long entryId, Long targetId, Long userId) {
        Entry dir = entryMapper.selectById(targetId);
        if (dir == null) throw new BusinessException("Entry does not exist");

        Entry entry = entryMapper.selectById(entryId);
        if (entry == null) throw new BusinessException("Entry does not exist");

        Entry copyEntry = Entry.builder()
                .driveId(entry.getDriveId())
                .userId(userId)
                .parentId(targetId)
                .storageId(entry.getStorageId())
                .entryName(entry.getEntryName())
                .entryType(entry.getEntryType())
                .fileSize(entry.getFileSize())
                .fileExt(entry.getFileExt())
                .status(entry.getStatus())
                .build();

        int count = entryMapper.insert(copyEntry);
        if (count != 1) throw new BusinessException("Move entry failed");
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
    public void deleteEntries(List<Long> entryIds, Long userId) {
        // 查询要删除的条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Entry::getId, entryIds)
                .eq(Entry::getStatus, UNDELETED);
        List<Entry> entries = entryMapper.selectList(queryWrapper);
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        // 分离文件夹和文件
        List<Long> folderIds = entries.stream()
                .filter(e -> e.getEntryType() != null && e.getEntryType() == FOLDER)
                .map(Entry::getId)
                .toList();

        // 收集所有需要删除的条目ID（包括文件夹的子项）
        Set<Long> allIds = new HashSet<>();
        entries.forEach(e -> allIds.add(e.getId()));

        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectDescendantsByFolderId(folderIds);
            children.forEach(c -> allIds.add(c.getId()));
        }

        // 设置删除信息并更新状态
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Entry::getId, allIds)
                .set(Entry::getStatus, DELETED)
                .set(Entry::getDeleterId, userId)
                .set(Entry::getDeletedAt, now)
                .set(Entry::getExpiredAt, now.plusDays(EXPIRE_DAYS));

        int count = entryMapper.update(updateWrapper);
        if (count != allIds.size()) throw new BusinessException("delete entries failed");
    }

    private boolean validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.length() > 100) return false;

        // 检查非法字符 \ / : * ? " < > |
        return !fileName.matches(".*[\\\\/:*?\"<>|].*");
    }
}
