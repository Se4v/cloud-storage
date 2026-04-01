package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.args.*;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Share;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.view.FolderTreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnterpriseService {
    @Autowired
    private EntryMapper entryMapper;
    @Autowired
    private DriveMapper driveMapper;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private ShareMapper shareMapper;

    private static final int ENTERPRISE_DRIVE = 2;
    private static final int UNDELETED = 1;
    private static final int DELETED = 2;
    private static final int FILE = 1;
    private static final int FOLDER = 2;
    private static final int EXPIRE_DAYS = 15;

    public List<Entry> listEntries(Long driveId, Long parentId) {
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getParentId, parentId)
                .eq(Entry::getDriveId, driveId)
                .eq(Entry::getStatus, UNDELETED);
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) return List.of();

        return entries;
    }

    public List<FolderTreeView> listFolders(Long driveId) {
        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getEntryType, FOLDER).eq(Entry::getDriveId, driveId);
        List<Entry> entries = entryMapper.selectList(entryQuery);
        if (entries == null || entries.isEmpty()) {
            // 返回只有个人空间根节点的列表
            FolderTreeView personalRoot = new FolderTreeView();
            personalRoot.setId(0L);
            personalRoot.setName("个人空间");
            personalRoot.setChildren(new ArrayList<>());
            return List.of(personalRoot);
        }

        Map<Long, FolderTreeView> nodeMap = new HashMap<>();
        List<FolderTreeView> roots = new ArrayList<>();

        for (Entry entry : entries) {
            FolderTreeView node = new FolderTreeView();
            node.setId(entry.getId());
            node.setName(entry.getEntryName());
            node.setChildren(new ArrayList<>());
            nodeMap.put(entry.getId(), node);
        }

        for (Entry entry : entries) {
            FolderTreeView node = nodeMap.get(entry.getId());
            Long parentId = entry.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                FolderTreeView parent = nodeMap.get(parentId);
                parent.getChildren().add(node);
            }
        }

        // 创建企业空间根节点
        Drive drive = driveMapper.selectById(driveId);
        FolderTreeView personalRoot = new FolderTreeView();
        personalRoot.setId(0L);
        personalRoot.setName(drive.getDriveName());
        personalRoot.setChildren(roots);

        return List.of(personalRoot);
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
        driveQuery.eq(Drive::getId, args.getDriveId())
                .eq(Drive::getDriveType, 2);
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
    public void moveEntries(MoveEntryArgs args, Long userId) {
        if (args.getTargetId() > 0) {
            Entry entry = entryMapper.selectById(args.getTargetId());
            if (entry == null) throw new BusinessException("Entry does not exist");
        }

        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Entry::getId, args.getIds())
                .set(Entry::getParentId, args.getTargetId());

        int count = entryMapper.update(updateWrapper);
        if (count != args.getIds().size()) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void copyEntry(CopyEntryArgs args, Long userId) {
        if (args.getTargetId() > 0) {
            Entry dir = entryMapper.selectById(args.getTargetId());
            if (dir == null) throw new BusinessException("Entry does not exist");
        }
        Entry entry = entryMapper.selectById(args.getId());
        if (entry == null) throw new BusinessException("Entry does not exist");

        // 目标目录下是否有同名文件
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entry::getParentId, entry.getParentId())
                .eq(Entry::getEntryName, entry.getEntryName());
        Entry sameNameEntry = entryMapper.selectOne(queryWrapper);
        String entryName = sameNameEntry == null ?
                entry.getEntryName() :
                entry.getEntryName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        Entry copyEntry = Entry.builder()
                .driveId(entry.getDriveId())
                .userId(userId)
                .parentId(args.getTargetId())
                .storageId(entry.getStorageId())
                .entryName(entryName)
                .entryType(entry.getEntryType())
                .fileSize(entry.getFileSize())
                .fileExt(entry.getFileExt())
                .status(entry.getStatus())
                .build();

        int count = entryMapper.insert(copyEntry);
        if (count != 1) throw new BusinessException("Move entry failed");

        LambdaUpdateWrapper<Storage> storageUpdate = new LambdaUpdateWrapper<>();
        storageUpdate.setIncrBy(Storage::getRefCount, 1).eq(Storage::getId, entry.getStorageId());
        int storageCount = storageMapper.update(storageUpdate);
        if (storageCount != 1) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void renameEntry(RenameEntryArgs args, Long userId) {
        Entry existedEntry = entryMapper.selectById(args.getId());
        if (existedEntry == null) throw new BusinessException("Entry does not exist");

        // 新文件名校验
        if (!validateFileName(args.getNewEntryName())) throw new BusinessException("Invalid file name");

        // 目标父目录下是否存在同名条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entry::getParentId, existedEntry.getParentId())
                .eq(Entry::getEntryName, args.getNewEntryName());
        Entry sameNameEntry = entryMapper.selectOne(queryWrapper);
        if (sameNameEntry != null) throw new BusinessException("Folder already exists");

        // 重命名
        LambdaUpdateWrapper<Entry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Entry::getEntryName, args.getNewEntryName()).eq(Entry::getId, args.getId());
        int count = entryMapper.update(updateWrapper);
        if (count != 1) throw new BusinessException("Rename entry failed");
    }

    public List<Entry> searchEntry(String targetName, Long driveId, Long userId) {
        // 文件名校验
        if (!validateFileName(targetName)) throw new BusinessException("Invalid file name");

        LambdaQueryWrapper<Entry> entryQuery = new LambdaQueryWrapper<>();
        entryQuery.eq(Entry::getDriveId, driveId)
                .like(Entry::getEntryName, targetName);
        List<Entry> entries = entryMapper.selectList(entryQuery);

        if (entries == null || entries.isEmpty()) throw new BusinessException("Search entry failed");

        return entries;
    }

    @Transactional
    public void deleteEntries(DeleteEntryArgs args, Long userId) {
        // 查询要删除的条目
        LambdaQueryWrapper<Entry> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Entry::getStatus, UNDELETED).in(Entry::getId, args.getIds());
        List<Entry> entries = entryMapper.selectList(queryWrapper);
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        // 分离文件夹和文件
        Map<Integer, List<Long>> groupedMap = entries.stream()
                .collect(Collectors.groupingBy(
                        Entry::getEntryType, // 按EntryType分组
                        Collectors.mapping(Entry::getId, Collectors.toList()) // 映射为id列表
                ));
        List<Long> fileIds = groupedMap.getOrDefault(FILE, new ArrayList<>());
        List<Long> folderIds = groupedMap.getOrDefault(FOLDER, new ArrayList<>());

        // 删除文件
        if (!fileIds.isEmpty()) {
            LambdaUpdateWrapper<Entry> fileUpdate = new LambdaUpdateWrapper<>();
            fileUpdate.set(Entry::getStatus, DELETED)
                    .set(Entry::getDeleterId, userId)
                    .set(Entry::getDeletedAt, LocalDateTime.now())
                    .set(Entry::getExpiredAt, LocalDateTime.now().plusDays(EXPIRE_DAYS))
                    .in(Entry::getId, fileIds);
            int count = entryMapper.update(fileUpdate);
            if (count != fileIds.size()) throw new BusinessException("delete entries failed");
        }

        // 删除文件夹及其所有子项
        if (!folderIds.isEmpty()) {
            List<Entry> children = entryMapper.selectDescendantsByFolderId(folderIds);
            List<Long> allFolderIds = new ArrayList<>(folderIds);
            allFolderIds.addAll(children.stream().map(Entry::getId).toList());

            LambdaUpdateWrapper<Entry> folderUpdate = new LambdaUpdateWrapper<>();
            folderUpdate.set(Entry::getStatus, DELETED)
                    .set(Entry::getDeleterId, userId)
                    .set(Entry::getDeletedAt, LocalDateTime.now())
                    .set(Entry::getExpiredAt, LocalDateTime.now().plusDays(EXPIRE_DAYS))
                    .in(Entry::getId, allFolderIds);
            int count = entryMapper.update(folderUpdate);
            if (count != allFolderIds.size()) throw new BusinessException("delete entries failed");
        }
    }

    @Transactional
    public void shareEntry(ShareEntryArgs args, Long userId) {
        Entry existedEntry = entryMapper.selectById(args.getId());
        if (existedEntry == null) throw new BusinessException("文件条目不存在");

        if (args.getLinkType() == 2 && args.getAccessCode().isBlank()) {
            throw new BusinessException("<UNK>");
        }

        Share link = Share.builder()
                .driveId(args.getDriveId())
                .entryId(existedEntry.getId())
                .entryType(existedEntry.getEntryType())
                .userId(userId)
                .linkName(args.getLinkName())
                .linkType(args.getLinkType())
                .linkKey(generateLinkKey())
                .accessCode(args.getAccessCode())
                .expiredAt(args.getExpireTime())
                .isDeleted(UNDELETED)
                .build();

        int count = shareMapper.insert(link);
        if (count != 1) throw new BusinessException("Create share link failed");
    }

    private boolean validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.length() > 100) return false;

        // 检查非法字符 \ / : * ? " < > |
        return !fileName.matches(".*[\\\\/:*?\"<>|].*");
    }

    private String generateLinkKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
}
