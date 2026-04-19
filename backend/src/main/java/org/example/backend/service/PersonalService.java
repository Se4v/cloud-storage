package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.http.Method;
import org.example.backend.aspect.LogContextHolder;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
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

    /**
     * 列出个人空间中的条目
     * @param driveId 个人空间ID
     * @param parentId 父目录ID
     * @return 条目列表
     */
    public List<Entry> listEntries(Long driveId, Long parentId) {
        Long currentUserId = SecurityUtils.getUserId();
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getUserId, currentUserId)
                        .eq(Entry::getParentId, parentId)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
        if (entries == null || entries.isEmpty()) return List.of();

        return entries;
    }

    /**
     * 列出个人空间的文件夹树
     * @param driveId 个人空间ID
     * @return 文件夹树结构
     */
    public List<FolderTreeResp> listFolders(Long driveId) {
        Long currentUserId = SecurityUtils.getUserId();
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getUserId, currentUserId)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getEntryType, DbConsts.ENTRY_TYPE_FOLDER)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
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

    /**
     * 在个人空间中创建文件夹
     * @param req 创建文件夹的请求参数
     */
    @Transactional
    public void createFolder(FolderCreationReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        // 校验目录是否存在且属于自己
        if (req.getParentId() > 0) {
            Entry parent = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, req.getParentId())
                            .eq(Entry::getDriveId, req.getDriveId())
                            .eq(Entry::getUserId, currentUserId)
                            .eq(Entry::getEntryType, DbConsts.ENTRY_TYPE_FOLDER)
                            .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
            if (parent == null) throw new BusinessException("目标目录不存在");
        }

        // 同空间下目标目录是否存在同名条目
        Entry sameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getParentId, req.getParentId())
                        .eq(Entry::getEntryName, req.getFolderName()));
        if (sameEntry != null) throw new BusinessException("目标目录下存在同名文件夹");

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName(req.getFolderName());
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("driveId", req.getDriveId());
        logMap.put("parentId", req.getParentId());
        logMap.put("folderName", req.getFolderName());
        LogContextHolder.addDetailProperty("mkdir", logMap);

        // 创建记录
        Entry folder = Entry.builder()
                .driveId(req.getDriveId())
                .userId(currentUserId)
                .parentId(req.getParentId())
                .storageId(0L)
                .entryName(req.getFolderName())
                .entryType(DbConsts.ENTRY_TYPE_FOLDER)
                .status(DbConsts.ENTRY_STATUS_UNDELETED)
                .build();
        int count = entryMapper.insert(folder);
        if (count != 1) throw new BusinessException("创建文件夹失败");
    }

    /**
     * 移动个人空间中的条目
     * @param req 移动条目的请求参数
     */
    @Transactional
    public void moveEntries(EntryMoveReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        // 校验当前文件夹是否存在且属于自己
        if (req.getTargetId() > 0) {
            Entry entry = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, req.getTargetId())
                            .eq(Entry::getDriveId, req.getDriveId())
                            .eq(Entry::getUserId, currentUserId)
                            .eq(Entry::getEntryType, DbConsts.ENTRY_TYPE_FOLDER)
                            .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
            if (entry == null) throw new BusinessException("目标目录不存在");
        }

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("批量移动" + req.getIds().size() + "个文件");
        List<Entry> entries = entryMapper.selectList(Wrappers.<Entry>lambdaQuery().in(Entry::getId, req.getIds()));
        List<Map<String, Object>> files = entries.stream()
                .map(file -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", file.getId());
                    map.put("nama", file.getEntryName());
                    map.put("type", file.getEntryType());
                    map.put("targetParentId", req.getTargetId());
                    return map;
                })
                .toList();
        LogContextHolder.addDetailProperty("batch_move", files);

        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getParentId, req.getTargetId())
                        .eq(Entry::getUserId, currentUserId)
                        .in(Entry::getId, req.getIds()));
        if (count != req.getIds().size()) throw new BusinessException("移动文件失败");
    }

    /**
     * 复制个人空间中的条目
     * @param req 复制条目的请求参数
     */
    @Transactional
    public void copyEntry(EntryCopyReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        if (req.getTargetId() > 0) {
            Entry parent = entryMapper.selectOne(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, req.getTargetId())
                            .eq(Entry::getDriveId, req.getDriveId())
                            .eq(Entry::getUserId, currentUserId)
                            .eq(Entry::getEntryType, DbConsts.ENTRY_TYPE_FOLDER)
                            .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
            if (parent == null) throw new BusinessException("目标目录不存在");
        }

        Entry entry = entryMapper.selectById(req.getId());
        if (entry == null) throw new BusinessException("该文件不存在");

        // 目标目录下是否有同名文件
        Entry sameNameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getParentId, entry.getParentId())
                        .eq(Entry::getEntryName, entry.getEntryName()));
        String entryName = sameNameEntry == null ?
                entry.getEntryName() :
                entry.getEntryName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName(entry.getEntryName());
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("driveId", req.getDriveId());
        logMap.put("parentId", entry.getParentId());
        logMap.put("entryId", entry.getId());
        logMap.put("entryName", entry.getEntryName());
        logMap.put("newEntryName", entryName);
        LogContextHolder.addDetailProperty("copy", logMap);

        Entry copyEntry = Entry.builder()
                .driveId(entry.getDriveId())
                .userId(currentUserId)
                .parentId(req.getTargetId())
                .storageId(entry.getStorageId())
                .entryName(entryName)
                .entryType(entry.getEntryType())
                .fileSize(entry.getFileSize())
                .fileExt(entry.getFileExt())
                .status(entry.getStatus())
                .build();

        int count = entryMapper.insert(copyEntry);
        if (count != 1) throw new BusinessException("复制文件失败");

        int storageCount = storageMapper.update(
                Wrappers.<Storage>lambdaUpdate()
                        .setIncrBy(Storage::getRefCount, 1)
                        .eq(Storage::getId, entry.getStorageId()));
        if (storageCount != 1) throw new BusinessException("复制文件失败");
    }

    /**
     * 重命名个人空间中的条目
     * @param req 重命名条目的请求参数
     */
    @Transactional
    public void renameEntry(EntryRenameReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        Entry existedEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, req.getId())
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getUserId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
        if (existedEntry == null) throw new BusinessException("该文件不存在");

        // 新文件名校验
        if (!validateFileName(req.getNewEntryName())) throw new BusinessException("新文件名无效");

        // 目标父目录下是否存在同名条目
        Entry sameNameEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getParentId, existedEntry.getParentId())
                        .eq(Entry::getEntryName, req.getNewEntryName()));
        if (sameNameEntry != null) throw new BusinessException("目标目录存在同名文件");

        LogContextHolder.setTargetId(existedEntry.getId());
        LogContextHolder.setTargetName(existedEntry.getEntryName());
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("driveId", req.getDriveId());
        logMap.put("parentId", existedEntry.getParentId());
        logMap.put("oldEntryName", existedEntry.getEntryName());
        logMap.put("newEntryName", req.getNewEntryName());
        LogContextHolder.addDetailProperty("rename", logMap);

        // 重命名
        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getEntryName, req.getNewEntryName())
                        .eq(Entry::getId, req.getId()));
        if (count != 1) throw new BusinessException("重命名文件失败");
    }

    /**
     * 删除个人空间中的条目
     * @param req 删除条目的请求参数
     */
    @Transactional
    public void deleteEntries(EntryDeletionReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        // 查询要删除的条目
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED)
                        .in(Entry::getId, req.getIds()));
        if (entries == null || entries.isEmpty()) throw new BusinessException("被删除的文件不存在");

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("批量删除" + req.getIds().size() + "个文件");
        List<Map<String, Object>> files = entries.stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("driveId", entry.getDriveId());
                    map.put("parentId", entry.getParentId());
                    map.put("entryId", entry.getId());
                    map.put("entryName", entry.getEntryName());
                    map.put("entryType", entry.getEntryType());
                    map.put("fileSize", entry.getFileSize());
                    return map;
                })
                .toList();
        LogContextHolder.addDetailProperty("batch_delete", files);

        int count = entryMapper.update(
                Wrappers.<Entry>lambdaUpdate()
                        .set(Entry::getStatus, DbConsts.ENTRY_STATUS_DELETED)
                        .set(Entry::getDeleterId, currentUserId)
                        .set(Entry::getDeletedAt, LocalDateTime.now())
                        .set(Entry::getExpiredAt, LocalDateTime.now().plusDays(EXPIRE_DAYS))
                        .eq(Entry::getUserId, currentUserId)
                        .in(Entry::getId, req.getIds()));
        if (count != entries.size()) throw new BusinessException("删除文件失败");
    }

    /**
     * 分享个人空间中的条目
     * @param req 分享条目的请求参数
     */
    @Transactional
    public void shareEntry(EntryShareReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        Entry existedEntry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, req.getId())
                        .eq(Entry::getDriveId, req.getDriveId())
                        .eq(Entry::getUserId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
        if (existedEntry == null) throw new BusinessException("该文件不存在");

        if (req.getLinkType() == 2 && req.getAccessCode().isBlank()) {
            throw new BusinessException("分享文件失败");
        }

        LogContextHolder.setTargetId(existedEntry.getId());
        LogContextHolder.setTargetName(existedEntry.getEntryName());
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("driveId", req.getDriveId());
        logMap.put("parentId", existedEntry.getParentId());
        logMap.put("entryId", existedEntry.getId());
        logMap.put("entryName", existedEntry.getEntryName());
        logMap.put("entryType", existedEntry.getEntryType());
        logMap.put("linkName", req.getLinkName());
        logMap.put("linkType", req.getLinkType());
        logMap.put("expireTime", req.getExpireTime().toString());
        LogContextHolder.addDetailProperty("share", logMap);

        Share link = Share.builder()
                .driveId(existedEntry.getDriveId())
                .entryId(existedEntry.getId())
                .entryType(existedEntry.getEntryType())
                .userId(currentUserId)
                .linkName(req.getLinkName())
                .linkType(req.getLinkType())
                .linkKey(generateLinkKey())
                .accessCode(req.getAccessCode())
                .expiredAt(req.getExpireTime())
                .isDeleted(DbConsts.DELETED_NO)
                .build();
        int count = shareMapper.insert(link);
        if (count != 1) throw new BusinessException("分享文件失败");
    }

    /**
     * 预览个人空间中的文件
     * @param id 文件条目ID
     * @param driveId 个人空间ID
     * @return 文件的预签名URL
     */
    public String preview(Long id, Long driveId) {
        Long currentUserId = SecurityUtils.getUserId();
        Entry entry = entryMapper.selectOne(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getId, id)
                        .eq(Entry::getDriveId, driveId)
                        .eq(Entry::getUserId, currentUserId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
        if (entry == null) throw new BusinessException("该文件不存在");

        if (!PREVIEW_EXT.contains(entry.getFileExt())) {
            throw new BusinessException("不支持预览该格式文件");
        }

        Storage storage = storageMapper.selectById(entry.getStorageId());
        if (storage == null) throw new BusinessException("预览文件失败");

        LogContextHolder.setTargetId(entry.getId());
        LogContextHolder.setTargetName(entry.getEntryName());
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("driveId", entry.getDriveId());
        logMap.put("parentId", entry.getParentId());
        logMap.put("entryId", entry.getId());
        logMap.put("entryName", entry.getEntryName());
        LogContextHolder.addDetailProperty("preview", logMap);

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

    /**
     * 验证文件名是否有效
     * @param fileName 文件名
     * @return 文件名有效返回true，否则返回false
     */
    private boolean validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.length() > 100) return false;
        // 检查非法字符 \ / : * ? " < > |
        return !fileName.matches(".*[\\\\/:*?\"<>|].*");
    }

    /**
     * 生成分享链接的唯一键
     * @return 生成的唯一键字符串
     */
    private String generateLinkKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
}
