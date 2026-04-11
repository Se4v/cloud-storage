package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.*;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.response.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.EnterpriseService;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {
    @Autowired
    private EnterpriseService enterpriseService;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private DownloadService downloadService;

    @PostMapping("/init-upload")
    public Result<?> initUpload(@RequestBody InitUploadArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        InitUploadView view = uploadService.initUpload(args, currentUserId);
        return Result.success("", view);
    }

    @PostMapping("/simple-upload")
    public Result<?> simpleUpload(@RequestBody SimpleUploadArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        SimpleUploadView view = uploadService.simpleUpload(args, currentUserId);
        return Result.success("", view);
    }

    @PostMapping("/upload-chunk")
    public Result<UploadChunkView> uploadChunk(@RequestBody UploadChunkArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        UploadChunkView view = uploadService.uploadChunk(args, currentUserId);
        return Result.success("", view);
    }

    @PostMapping("/merge-chunks")
    public Result<MergeChunksView> mergeChunks(@RequestBody MergeChunksArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        MergeChunksView view = uploadService.mergeChunks(args, currentUserId);
        return Result.success("", view);
    }

    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody DownloadArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        String fileName = downloadService.getDownloadFileName(args);

        StreamingResponseBody stream = downloadService.download(args, currentUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok().headers(headers).body(stream);
    }

    @GetMapping
    public Result<?> listEntries(@RequestParam Long driveId, @RequestParam Long parentId) {
        Long currentOrgId = SecurityUtil.getOrgId();
        List<Entry> entries = enterpriseService.listEntries(driveId, parentId, currentOrgId);
        List<EntryView> views = entries.stream().map(entry -> EntryView.builder()
                .id(entry.getId())
                .name(entry.getEntryName())
                .type(entry.getEntryType())
                .size(entry.getFileSize())
                .createTime(entry.getCreatedAt())
                .build()).toList();
        return Result.success("", views);
    }

    @GetMapping("/folder")
    public Result<?> listFolders(@RequestParam Long driveId) {
        Long currentOrgId = SecurityUtil.getOrgId();
        List<FolderTreeView> views = enterpriseService.listFolders(driveId, currentOrgId);
        return Result.success(views);
    }

    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody CreateFolderArgs args) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.createFolder(args, currentUserId, currentOrgId);
        return Result.success();
    }

    @PostMapping("/move")
    public Result<?> moveEntries(@RequestBody MoveEntryArgs args) {
        Long currentOrgId = SecurityUtil.getOrgId();
        enterpriseService.moveEntries(args, currentOrgId);
        return Result.success();
    }

    @PostMapping("/copy")
    public Result<?> copyEntry(@RequestBody CopyEntryArgs args) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.copyEntry(args, currentUserId, currentOrgId);
        return Result.success();
    }

    @PostMapping("/rename")
    public Result<?> renameEntry(@RequestBody RenameEntryArgs args) {
        Long currentOrgId = SecurityUtil.getOrgId();
        enterpriseService.renameEntry(args, currentOrgId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody DeleteEntryArgs args) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.deleteEntries(args, currentUserId, currentOrgId);
        return Result.success();
    }

    @PostMapping("/share")
    public Result<?> shareEntry(@RequestBody ShareEntryArgs args) {
        Long currentOrgId = SecurityUtil.getOrgId();
        Long currentUserId = SecurityUtil.getUserId();
        enterpriseService.shareEntry(args, currentUserId, currentOrgId);
        return Result.success();
    }

    @GetMapping("/preview")
    public Result<?> preview(@RequestParam("id") Long id, @RequestParam("driveId") Long driveId) {
        Long currentOrgId = SecurityUtil.getOrgId();
        String url = enterpriseService.preview(id, driveId, currentOrgId);
        return Result.success("", url);
    }
}
