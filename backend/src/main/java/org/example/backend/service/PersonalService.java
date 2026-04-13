package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.http.Method;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Share;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.request.file.*;
import org.example.backend.model.response.file.FolderTreeResp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PersonalService {
    private final EntryMapper entryMapper;
    private final StorageMapper storageMapper;
    private final ShareMapper shareMapper;
    private final MinioAsyncClient minioClient;

    private static final int UNDELETED = 1;
    private static final int DELETED = 2;
    private static final int FOLDER = 2;
    private static final int EXPIRE_DAYS = 15;
    private static final List<String> PREVIEW_EXT = Arrays.asList(
            // 图像格式
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico",
            // 文档格式
            "pdf",
            // 文本格式
            "txt", "md", "json", "xml", "csv", "html", "htm"
    );

    public PersonalService(EntryMapper entryMapper, StorageMapper storageMapper,
                           ShareMapper shareMapper, MinioAsyncClient minioClient) {
        this.entryMapper = entryMapper;
        this.storageMapper = storageMapper;
        this.shareMapper = shareMapper;
        this.minioClient = minioClient;
    }

    public List<Entry> listEntries(Long driveId, Long parentId, Long userId) {
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getUserId, userId)
                        .eq(Entry::getParentId, parentId)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getStatus, UNDELETED));
        if (entries == null || entries.isEmpty()) return List.of();

        return entries;
    }

    public List<FolderTreeResp> listFolders(Long driveId, Long userId) {
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getUserId, userId)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getEntryType, FOLDER)
                        .eq(Entry::getStatus, UNDELETED));
        if (entries == null || entries.isEmpty()) {
            // 返回只有个人空间根节点的列表
            FolderTreeResp personalRoot = new FolderTreeResp();
            personalRoot.setId(0L);
            personalRoot.setName("个人空间");
            personalRoot.setChildren(new ArrayList<>());
            return List.of(personalRoot);
        }
        
        Map<Long, FolderTreeResp> nodeMap = new HashMap<>();
        List<FolderTreeResp> roots = new ArrayList<>();

        for (Entry entry : entries) {
            FolderTreeResp node = new FolderTreeResp();
            node.setId(entry.getId());
            node.setName(entry.getEntryName());
            node.setChildren(new ArrayList<>());
            nodeMap.put(entry.getId(), node);
        }

        for (Entry entry : entries) {
            FolderTreeResp node = nodeMap.get(entry.getId());
            Long parentId = entry.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                FolderTreeResp parent = nodeMap.get(parentId);
                parent.getChildren().add(node);
            }
        }
        
        // 创建个人空间根节点
        FolderTreeResp personalRoot = new FolderTreeResp();
        personalRoot.setId(0L);
        personalRoot.setName("个人空间");
        personalRoot.setChildren(roots);
        
        return List.of(personalRoot);
    }

    @Transactional
    public void createFolder(FolderCreationReq req, Long userId) {
        // 校验目录是否存在且属于自己
        if (req.getParentId() > 0) {
            Entry parent = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, req.getParentId())
                            .eq(Entry::getDriveId, req.getDriveId())
                            .eq(Entry::getUserId, userId)
                            .eq(Entry::getEntryType, FOLDER)
                            .eq(Entry::getStatus, UNDELETED));
            if (parent == null) throw new BusinessException("<UNK>");
        }

        // 同空间下目标目录是否存在同名条目
        Entry sameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getParentId, req.getParentId())
                        .eq(Entry::getEntryName, req.getFolderName()));
        if (sameEntry != null) throw new BusinessException("Folder already exists");

        // 创建记录
        Entry folder = Entry.builder()
                .driveId(req.getDriveId())
                .userId(userId)
                .parentId(req.getParentId())
                .storageId(0L)
                .entryName(req.getFolderName())
                .entryType(FOLDER)
                .status(UNDELETED)
                .build();
        int count = entryMapper.insert(folder);
        if (count != 1) throw new BusinessException("Create folder failed");
    }

    @Transactional
    public void moveEntries(EntryMoveReq req, Long userId) {
        // 校验当前文件夹是否存在且属于自己
        if (req.getTargetId() > 0) {
            Entry entry = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, req.getTargetId())
                            .eq(Entry::getDriveId, req.getDriveId())
                            .eq(Entry::getUserId, userId)
                            .eq(Entry::getEntryType, FOLDER)
                            .eq(Entry::getStatus, UNDELETED));
            if (entry == null) throw new BusinessException("Entry does not exist");
        }

        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getParentId, req.getTargetId())
                        .eq(Entry::getUserId, userId)
                        .in(Entry::getId, req.getIds()));
        if (count != req.getIds().size()) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void copyEntry(EntryCopyReq req, Long userId) {
        if (req.getTargetId() > 0) {
            Entry parent = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, req.getTargetId())
                            .eq(Entry::getDriveId, req.getDriveId())
                            .eq(Entry::getUserId, userId)
                            .eq(Entry::getEntryType, FOLDER)
                            .eq(Entry::getStatus, UNDELETED));
            if (parent == null) throw new BusinessException("<UNK>");
        }

        Entry entry = entryMapper.selectById(req.getId());
        if (entry == null) throw new BusinessException("Entry does not exist");

        // 目标目录下是否有同名文件
        Entry sameNameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getParentId, entry.getParentId())
                        .eq(Entry::getEntryName, entry.getEntryName()));
        String entryName = sameNameEntry == null ?
                entry.getEntryName() :
                entry.getEntryName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        Entry copyEntry = Entry.builder()
                .driveId(entry.getDriveId())
                .userId(userId)
                .parentId(req.getTargetId())
                .storageId(entry.getStorageId())
                .entryName(entryName)
                .entryType(entry.getEntryType())
                .fileSize(entry.getFileSize())
                .fileExt(entry.getFileExt())
                .status(entry.getStatus())
                .build();

        int count = entryMapper.insert(copyEntry);
        if (count != 1) throw new BusinessException("Move entry failed");

        int storageCount = storageMapper.update(
                Wrappers.<Storage>lambdaUpdate()
                        .setIncrBy(Storage::getRefCount, 1)
                        .eq(Storage::getId, entry.getStorageId()));
        if (storageCount != 1) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void renameEntry(EntryRenameReq req, Long userId) {
        Entry existedEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, req.getId())
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getUserId, userId)
                        .eq(Entry::getStatus, UNDELETED));
        if (existedEntry == null) throw new BusinessException("Entry does not exist");

        // 新文件名校验
        if (!validateFileName(req.getNewEntryName())) throw new BusinessException("Invalid file name");

        // 目标父目录下是否存在同名条目
        Entry sameNameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getParentId, existedEntry.getParentId())
                        .eq(Entry::getEntryName, req.getNewEntryName()));
        if (sameNameEntry != null) throw new BusinessException("Folder already exists");

        // 重命名
        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getEntryName, req.getNewEntryName())
                        .eq(Entry::getId, req.getId()));
        if (count != 1) throw new BusinessException("Rename entry failed");
    }

    @Transactional
    public void deleteEntries(EntryDeletionReq req, Long userId) {
        // 查询要删除的条目
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getStatus, UNDELETED)
                        .in(Entry::getId, req.getIds()));
        if (entries == null || entries.isEmpty()) throw new BusinessException("entry does not exist");

        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getStatus, DELETED)
                        .set(Entry::getDeleterId, userId)
                        .set(Entry::getDeletedAt, LocalDateTime.now())
                        .set(Entry::getExpiredAt, LocalDateTime.now().plusDays(EXPIRE_DAYS))
                        .eq(Entry::getUserId, userId)
                        .in(Entry::getId, req.getIds()));
        if (count != entries.size()) throw new BusinessException("Delete entry failed");
    }

    @Transactional
    public void shareEntry(EntryShareReq req, Long userId) {
        Entry existedEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, req.getId())
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getUserId, userId)
                        .eq(Entry::getStatus, UNDELETED));
        if (existedEntry == null) throw new BusinessException("文件条目不存在");

        if (req.getLinkType() == 2 && req.getAccessCode().isBlank()) {
            throw new BusinessException("<UNK>");
        }

        Share link = Share.builder()
                .driveId(existedEntry.getDriveId())
                .entryId(existedEntry.getId())
                .entryType(existedEntry.getEntryType())
                .userId(userId)
                .linkName(req.getLinkName())
                .linkType(req.getLinkType())
                .linkKey(generateLinkKey())
                .accessCode(req.getAccessCode())
                .expiredAt(req.getExpireTime())
                .isDeleted(0)
                .build();
        int count = shareMapper.insert(link);
        if (count != 1) throw new BusinessException("Create share link failed");
    }

    public String preview(Long id, Long driveId, Long userId) {
        Entry entry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, id)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getUserId, userId)
                        .eq(Entry::getStatus, UNDELETED));
        if (entry == null) throw new BusinessException("Entry does not exist");

        if (!PREVIEW_EXT.contains(entry.getFileExt())) {
            throw new BusinessException("Invalid file ext");
        }

        Storage storage = storageMapper.selectById(entry.getStorageId());
        if (storage == null) throw new BusinessException("Storage does not exist");

        String url;
        try {
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(storage.getBucketName())
                            .object(storage.getObjectKey())
                            .expiry(10, TimeUnit.MINUTES)
                            .extraQueryParams(Map.of(
                                    "response-content-disposition",
                                    "inline; filename=\"" + entry.getEntryName() + "\""
                            ))
                            .build());
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }

        return url;
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
