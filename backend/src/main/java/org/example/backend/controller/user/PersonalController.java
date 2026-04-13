package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.request.file.*;
import org.example.backend.model.response.drive.PersonalDriveUsageResp;
import org.example.backend.model.response.file.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.DriveService;
import org.example.backend.service.PersonalService;
import org.example.backend.service.UploadService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
    private final PersonalService personalService;
    private final UploadService uploadService;
    private final DownloadService downloadService;
    private final DriveService driveService;

    public PersonalController(PersonalService personalService, UploadService uploadService,
                              DownloadService downloadService, DriveService driveService) {
        this.personalService = personalService;
        this.uploadService = uploadService;
        this.downloadService = downloadService;
        this.driveService = driveService;
    }

    @PostMapping("/init-upload")
    public Result<?> initUpload(@RequestBody UploadInitReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        UploadInitResp resp = uploadService.initUpload(req, currentUserId);
        return Result.success(resp);
    }

    @PostMapping("/simple-upload")
    public Result<?> directUpload(@RequestBody DirectUploadReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        DirectUploadResp resp = uploadService.directUpload(req, currentUserId);
        return Result.success(resp);
    }

    @PostMapping("/upload-chunk")
    public Result<?> uploadChunk(@RequestBody ChunkUploadReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        ChunkUploadResp resp = uploadService.uploadChunk(req, currentUserId);
        return Result.success(resp);
    }

    @PostMapping("/merge-chunks")
    public Result<?> mergeChunks(@RequestBody ChunkMergeReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        ChunkMergeResp resp = uploadService.mergeChunks(req, currentUserId);
        return Result.success(resp);
    }

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

    @GetMapping
    public Result<?> listEntries(@RequestParam Long driveId, @RequestParam Long parentId) {
        Long currentUserId = SecurityUtil.getUserId();
        List<Entry> entries = personalService.listEntries(driveId, parentId, currentUserId);
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
        Long currentUserId = SecurityUtil.getUserId();
        List<FolderTreeResp> resp = personalService.listFolders(driveId, currentUserId);
        return Result.success(resp);
    }

    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody FolderCreationReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        personalService.createFolder(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/move")
    public Result<?> moveEntries(@RequestBody EntryMoveReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        personalService.moveEntries(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/copy")
    public Result<?> copyEntry(@RequestBody EntryCopyReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        personalService.copyEntry(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/rename")
    public Result<?> renameEntry(@RequestBody EntryRenameReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        personalService.renameEntry(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody EntryDeletionReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        personalService.deleteEntries(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/share")
    public Result<?> shareEntry(@RequestBody EntryShareReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        personalService.shareEntry(req, currentUserId);
        return Result.success();
    }

    @GetMapping("/preview")
    public Result<?> preview(@RequestParam("id") Long id, @RequestParam("driveId") Long driveId) {
        Long currentUserId = SecurityUtil.getUserId();
        String url = personalService.preview(id, driveId, currentUserId);
        return Result.success(url);
    }

    @GetMapping("/usage")
    public Result<?> getPersonalDriveUsage() {
        Long currentUserId = SecurityUtil.getUserId();
        Drive drive = driveService.getPersonalDriveUsage(currentUserId);
        PersonalDriveUsageResp resp = PersonalDriveUsageResp.builder()
                .usedQuota(drive.getUsedQuota())
                .totalQuota(drive.getTotalQuota())
                .build();
        return Result.success(resp);
    }

    @GetMapping("/id")
    public Result<?> getPersonalDriveId() {
        Long currentUserId = SecurityUtil.getUserId();
        Long personalDriveId = driveService.getPersonalDriveId(currentUserId);
        return Result.success(personalDriveId);
    }
}
