package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
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

    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/init-upload")
    public Result<?> initUpload(@RequestBody UploadInitReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        UploadInitResp resp = uploadService.initUpload(req, currentUserId);
        return Result.success(resp);
    }

    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/simple-upload")
    public Result<?> directUpload(@RequestBody DirectUploadReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        DirectUploadResp resp = uploadService.directUpload(req, currentUserId);
        return Result.success(resp);
    }

    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/upload-chunk")
    public Result<?> uploadChunk(@RequestBody ChunkUploadReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        ChunkUploadResp resp = uploadService.uploadChunk(req, currentUserId);
        return Result.success(resp);
    }

    @PreAuthorize("hasAuthority('file:upload')")
    @PostMapping("/merge-chunks")
    public Result<?> mergeChunks(@RequestBody ChunkMergeReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        ChunkMergeResp resp = uploadService.mergeChunks(req, currentUserId);
        return Result.success(resp);
    }

    @PreAuthorize("hasAuthority('file:dowanload')")
    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody EntryDownloadReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        String fileName = downloadService.getDownloadFileName(req);

        StreamingResponseBody stream = downloadService.download(req, currentUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok().headers(headers).body(stream);
    }

    @PreAuthorize("hasAuthority('file:list')")
    @GetMapping
    public Result<?> listEntries(@RequestParam Long driveId, @RequestParam Long parentId) {
        Long currentOrgId = SecurityUtil.getOrgId();
        List<Entry> entries = enterpriseService.listEntries(driveId, parentId, currentOrgId);
        List<EntryResp> resp = entries.stream().map(entry -> EntryResp.builder()
                .id(entry.getId())
                .name(entry.getEntryName())
                .type(entry.getEntryType())
                .size(entry.getFileSize())
                .createTime(entry.getCreatedAt())
                .build()).toList();
        return Result.success(resp);
    }


    @GetMapping("/folder")
    public Result<?> listFolders(@RequestParam Long driveId) {
        Long currentOrgId = SecurityUtil.getOrgId();
        List<FolderTreeResp> resp = enterpriseService.listFolders(driveId, currentOrgId);
        return Result.success(resp);
    }

    @PreAuthorize("hasAuthority('file:mkdir')")
    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody FolderCreationReq req) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.createFolder(req, currentUserId, currentOrgId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:move')")
    @PostMapping("/move")
    public Result<?> moveEntries(@RequestBody EntryMoveReq req) {
        Long currentOrgId = SecurityUtil.getOrgId();
        enterpriseService.moveEntries(req, currentOrgId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:copy')")
    @PostMapping("/copy")
    public Result<?> copyEntry(@RequestBody EntryCopyReq req) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.copyEntry(req, currentUserId, currentOrgId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:rename')")
    @PostMapping("/rename")
    public Result<?> renameEntry(@RequestBody EntryRenameReq req) {
        Long currentOrgId = SecurityUtil.getOrgId();
        enterpriseService.renameEntry(req, currentOrgId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:delete')")
    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody EntryDeletionReq req) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.deleteEntries(req, currentUserId, currentOrgId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:share')")
    @PostMapping("/share")
    public Result<?> shareEntry(@RequestBody EntryShareReq req) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.shareEntry(req, currentUserId, currentOrgId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:view')")
    @GetMapping("/preview")
    public Result<?> preview(@RequestParam("id") Long id, @RequestParam("driveId") Long driveId) {
        Long currentOrgId = SecurityUtil.getOrgId();
        String url = enterpriseService.preview(id, driveId, currentOrgId);
        return Result.success(url);
    }
}
