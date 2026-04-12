package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.http.Method;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Share;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.request.file.*;
import org.example.backend.model.response.file.FolderTreeView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class EnterpriseService {
    private final EntryMapper entryMapper;
    private final DriveMapper driveMapper;
    private final StorageMapper storageMapper;
    private final ShareMapper shareMapper;
    private final MinioAsyncClient minioClient;

    public EnterpriseService(EntryMapper entryMapper, DriveMapper driveMapper, StorageMapper storageMapper,
                             ShareMapper shareMapper, MinioAsyncClient minioClient) {
        this.entryMapper = entryMapper;
        this.driveMapper = driveMapper;
        this.storageMapper = storageMapper;
        this.shareMapper = shareMapper;
        this.minioClient = minioClient;
    }

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

    public List<Entry> listEntries(Long driveId, Long parentId, Long orgId) {
        if (!isMemberOfOrganization(driveId, orgId)) throw new BusinessException("未授权操作");

        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getParentId, parentId)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getStatus, UNDELETED));
        if (entries == null || entries.isEmpty()) return List.of();

        return entries;
    }

    public List<FolderTreeView> listFolders(Long driveId, Long orgId) {
        if (!isMemberOfOrganization(driveId, orgId)) throw new BusinessException("未授权操作");

        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getEntryType, FOLDER)
                        .eq(Entry::getStatus, UNDELETED)
        );
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
    public void createFolder(CreateFolderArgs args, Long userId, Long orgId) {
        if (!isMemberOfOrganization(args.getDriveId(), orgId)) throw new BusinessException("未授权操作");

        if (args.getParentId() > 0) {
            Entry dir = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, args.getParentId())
                            .eq(Entry::getDriveId, args.getDriveId())
                            .eq(Entry::getEntryType, FOLDER)
                            .eq(Entry::getStatus, UNDELETED));
            if (dir == null) throw new BusinessException("<UNK>");
        }

        // 目标父目录下是否存在同名条目
        Entry sameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                    .eq(Entry::getDriveId, args.getDriveId())
                    .eq(Entry::getParentId, args.getParentId())
                    .eq(Entry::getEntryName, args.getFolderName()));
        if (sameEntry != null) throw new BusinessException("Folder already exists");

        // 创建记录
        Entry folder = Entry.builder()
                .driveId(args.getDriveId())
                .userId(userId)
                .parentId(args.getParentId())
                .storageId(0L)
                .entryName(args.getFolderName())
                .entryType(FOLDER)
                .status(UNDELETED)
                .build();
        int count = entryMapper.insert(folder);
        if (count != 1) throw new BusinessException("Create folder failed");
    }

    @Transactional
    public void moveEntries(MoveEntryArgs args, Long orgId) {
        if (!isMemberOfOrganization(args.getDriveId(), orgId)) throw new BusinessException("未授权操作");

        if (args.getTargetId() > 0) {
            Entry entry = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, args.getTargetId())
                            .eq(Entry::getDriveId, args.getDriveId())
                            .eq(Entry::getEntryType, FOLDER)
                            .eq(Entry::getStatus, UNDELETED));
            if (entry == null) throw new BusinessException("Entry does not exist");
        }

        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getParentId, args.getTargetId())
                        .eq(Entry::getDriveId, args.getDriveId())
                        .in(Entry::getId, args.getIds()));
        if (count != args.getIds().size()) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void copyEntry(CopyEntryArgs args, Long userId, Long orgId) {
        if (!isMemberOfOrganization(args.getDriveId(), orgId)) throw new BusinessException("未授权操作");

        if (args.getTargetId() > 0) {
            Entry dir = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, args.getTargetId())
                            .eq(Entry::getDriveId, args.getDriveId())
                            .eq(Entry::getEntryType, FOLDER)
                            .eq(Entry::getStatus, UNDELETED));
            if (dir == null) throw new BusinessException("Entry does not exist");
        }

        Entry entry = entryMapper.selectById(args.getId());
        if (entry == null) throw new BusinessException("Entry does not exist");

        // 同个空间的目标目录下是否有同名文件
        Entry sameNameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, args.getDriveId())
                        .eq(Entry::getParentId, entry.getParentId())
                        .eq(Entry::getEntryName, entry.getEntryName())
                        .eq(Entry::getStatus, UNDELETED)
        );
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

        int storageCount = storageMapper.update(
                Wrappers.<Storage>lambdaUpdate()
                        .setIncrBy(Storage::getRefCount, 1)
                        .eq(Storage::getId, entry.getStorageId()));
        if (storageCount != 1) throw new BusinessException("Move entry failed");
    }

    @Transactional
    public void renameEntry(RenameEntryArgs args, Long orgId) {
        if (!isMemberOfOrganization(args.getDriveId(), orgId)) throw new BusinessException("未授权操作");

        Entry existedEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, args.getId())
                        .eq(Entry::getDriveId, args.getDriveId())
                        .eq(Entry::getStatus, UNDELETED));
        if (existedEntry == null) throw new BusinessException("Entry does not exist");

        // 新文件名校验
        if (!validateFileName(args.getNewEntryName())) throw new BusinessException("Invalid file name");

        // 同个空间的目标目录下是否存在同名条目
        Entry sameNameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, args.getDriveId())
                        .eq(Entry::getParentId, existedEntry.getParentId())
                        .eq(Entry::getEntryName, args.getNewEntryName())
                        .eq(Entry::getStatus, UNDELETED));
        if (sameNameEntry != null) throw new BusinessException("Folder already exists");

        // 重命名
        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getEntryName, args.getNewEntryName())
                        .eq(Entry::getId, args.getId()));
        if (count != 1) throw new BusinessException("Rename entry failed");
    }

    @Transactional
    public void deleteEntries(DeleteEntryArgs args, Long userId, Long orgId) {
        if (!isMemberOfOrganization(args.getDriveId(), orgId)) throw new BusinessException("未授权操作");

        // 查询要删除的条目
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, args.getDriveId())
                        .eq(Entry::getStatus, UNDELETED)
                        .in(Entry::getId, args.getIds()));
        if (entries == null || entries.isEmpty() || entries.size() != args.getIds().size()) {
            throw new BusinessException("entry does not exist");
        }

        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getStatus, DELETED)
                        .set(Entry::getDeleterId, userId)
                        .set(Entry::getDeletedAt, LocalDateTime.now())
                        .set(Entry::getExpiredAt, LocalDateTime.now().plusDays(EXPIRE_DAYS))
                        .eq(Entry::getDriveId, args.getDriveId())
                        .in(Entry::getId, args.getIds()));
        if (count != entries.size()) throw new BusinessException("Delete entry failed");
    }

    @Transactional
    public void shareEntry(ShareEntryArgs args, Long userId, Long orgId) {
        if (!isMemberOfOrganization(args.getDriveId(), orgId)) throw new BusinessException("未授权操作");

        Entry existedEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, args.getId())
                        .eq(Entry::getDriveId, args.getDriveId())
                        .eq(Entry::getStatus, UNDELETED));
        if (existedEntry == null) throw new BusinessException("文件条目不存在");

        if (args.getLinkType() == 2 && args.getAccessCode().isBlank()) {
            throw new BusinessException("<UNK>");
        }

        Share link = Share.builder()
                .driveId(existedEntry.getDriveId())
                .entryId(existedEntry.getId())
                .entryType(existedEntry.getEntryType())
                .userId(userId)
                .linkName(args.getLinkName())
                .linkType(args.getLinkType())
                .linkKey(generateLinkKey())
                .accessCode(args.getAccessCode())
                .expiredAt(args.getExpireTime())
                .isDeleted(0)
                .build();
        int count = shareMapper.insert(link);
        if (count != 1) throw new BusinessException("Create share link failed");
    }

    public String preview(Long id, Long driveId, Long orgId) {
        if (!isMemberOfOrganization(driveId, orgId)) throw new BusinessException("未授权操作");

        Entry entry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, id)
                        .eq(Entry::getDriveId, driveId)
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

        // 检查非法字符
        return !fileName.matches(".*[\\\\/:*?\"<>|].*");
    }

    private String generateLinkKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    private boolean isMemberOfOrganization(Long driveId, Long orgId) {
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getNodeId, orgId)
                        .eq(Drive::getDriveType, 2));
        if (drive == null) throw new BusinessException("当前部门未分配企业空间");
        Long realDriveId = drive.getId();

        return realDriveId.equals(driveId);
    }
}