package org.example.backend.controller.user;

import org.example.backend.common.annotation.OperationLog;
import org.example.backend.common.result.Result;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.request.file.*;
import org.example.backend.model.response.file.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.EnterpriseService;
import org.example.backend.service.UploadService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {
    private final EnterpriseService enterpriseService;
    private final UploadService uploadService;
    private final DownloadService downloadService;

    public EnterpriseController(EnterpriseService enterpriseService, UploadService uploadService,
                                DownloadService downloadService) {
        this.enterpriseService = enterpriseService;
        this.uploadService = uploadService;
        this.downloadService = downloadService;
    }

    /**
     * 上传初始化
     * @param req 上传初始化请求参数
     * @return 上传初始化响应结果
     */
    @OperationLog(module = "企业空间模块", action = "UPLOAD", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/init-upload")
    public Result<?> initUpload(@RequestBody UploadInitReq req) {
        UploadInitResp resp = uploadService.initUpload(req);
        return Result.success(resp);
    }

    /**
     * 普通单文件上传
     * @param req 普通上传请求参数
     * @return 上传完成响应结果
     */
    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/simple-upload")
    public Result<?> directUpload(@RequestBody DirectUploadReq req) {
        DirectUploadResp resp = uploadService.directUpload(req);
        return Result.success(resp);
    }

    /**
     * 上传文件分片
     * @param req 分片上传请求参数
     * @return 分片上传响应结果
     */
    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/upload-chunk")
    public Result<?> uploadChunk(@RequestBody ChunkUploadReq req) {
        ChunkUploadResp resp = uploadService.uploadChunk(req);
        return Result.success(resp);
    }

    /**
     * 合并文件分片
     * @param req 分片合并请求参数
     * @return 合并完成响应结果
     */
    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/merge-chunks")
    public Result<?> mergeChunks(@RequestBody ChunkMergeReq req) {
        ChunkMergeResp resp = uploadService.mergeChunks(req);
        return Result.success(resp);
    }

    /**
     * 文件下载
     * @param req 文件下载请求参数
     * @return 文件流响应实体
     */
    @OperationLog(module = "企业空间模块", action = "DOWNLOAD", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:dowanload')")
    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody EntryDownloadReq req) {
        String fileName = downloadService.getDownloadFileName(req);

        StreamingResponseBody stream = downloadService.download(req, true);

        HttpHeaders headers = new HttpHeaders();
        if (req.getIds().size() == 1) {
            String mimeType = downloadService.getFileContentType(req);
            headers.setContentType(MediaType.parseMediaType(mimeType));
        } else if (req.getIds().size() > 1) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok().headers(headers).body(stream);
    }

    /**
     * 获取文件条目列表
     * @param driveId 空间ID
     * @param parentId 父目录ID
     * @return 文件条目列表
     */
    @PreAuthorize("hasAuthority('file:list')")
    @GetMapping
    public Result<?> listEntries(@RequestParam Long driveId, @RequestParam Long parentId) {
        List<Entry> entries = enterpriseService.listEntries(driveId, parentId);
        List<EntryResp> resp = entries.stream().map(entry -> EntryResp.builder()
                .id(entry.getId())
                .name(entry.getEntryName())
                .type(entry.getEntryType())
                .size(entry.getFileSize())
                .createTime(entry.getCreatedAt())
                .build()).toList();
        return Result.success(resp);
    }

    /**
     * 获取文件夹树形列表
     * @param driveId 网盘ID
     * @return 文件夹树形结构列表
     */
    @GetMapping("/folder")
    public Result<?> listFolders(@RequestParam Long driveId) {
        List<FolderTreeResp> resp = enterpriseService.listFolders(driveId);
        return Result.success(resp);
    }

    /**
     * 创建文件夹
     * @param req 文件夹创建请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "企业空间模块", action = "MKDIR", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:mkdir')")
    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody FolderCreationReq req) {
        enterpriseService.createFolder(req);
        return Result.success();
    }

    /**
     * 批量移动文件条目
     * @param req 文件移动请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "企业空间模块", action = "MOVE", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:move')")
    @PostMapping("/move")
    public Result<?> moveEntries(@RequestBody EntryMoveReq req) {
        enterpriseService.moveEntries(req);
        return Result.success();
    }

    /**
     * 复制文件条目
     * @param req 文件复制请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "企业空间模块", action = "COPY", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:copy')")
    @PostMapping("/copy")
    public Result<?> copyEntry(@RequestBody EntryCopyReq req) {
        enterpriseService.copyEntry(req);
        return Result.success();
    }

    /**
     * 重命名文件条目
     * @param req 重命名请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "企业空间模块", action = "RENAME", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:rename')")
    @PostMapping("/rename")
    public Result<?> renameEntry(@RequestBody EntryRenameReq req) {
        enterpriseService.renameEntry(req);
        return Result.success();
    }

    /**
     * 批量删除文件条目
     * @param req 删除请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "企业空间模块", action = "DELETE", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:delete')")
    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody EntryDeletionReq req) {
        enterpriseService.deleteEntries(req);
        return Result.success();
    }

    /**
     * 创建文件条目分享链接
     * @param req 文件分享请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "企业空间模块", action = "SHARE", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:share')")
    @PostMapping("/share")
    public Result<?> shareEntry(@RequestBody EntryShareReq req) {
        enterpriseService.shareEntry(req);
        return Result.success();
    }

    /**
     * 获取文件预览地址
     * @param id 文件ID
     * @param driveId 空间ID
     * @return 文件预览URL
     */
    @OperationLog(module = "企业空间模块", action = "PREVIEW", targetType = "FILE")
    @PreAuthorize("hasAuthority('file:view')")
    @GetMapping("/preview")
    public Result<?> preview(@RequestParam("id") Long id, @RequestParam("driveId") Long driveId) {
        String url = enterpriseService.preview(id, driveId);
        return Result.success(url);
    }
}
