package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.*;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.view.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.DriveService;
import org.example.backend.service.PersonalService;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
    @Autowired
    private PersonalService personalService;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private DriveService driveService;

    Long userId = 2034965772877197313L;

    @PostMapping("/init-upload")
    public Result<InitUploadView> initUpload(@RequestBody InitUploadArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        // return Result.success(uploadService.initUpload(args, userDetails.getUserId()));
        return Result.success(uploadService.initUpload(args, userId));
    }

    @PostMapping("/simple-upload")
    public Result<SimpleUploadView> simpleUpload(@RequestBody SimpleUploadArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // return Result.success(uploadService.simpleUpload(args, userDetails.getUserId()));

        return Result.success(uploadService.simpleUpload(args, userId));
    }

    @PostMapping("/upload-chunk")
    public Result<UploadChunkView> uploadChunk(@RequestBody UploadChunkArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // return Result.success(uploadService.uploadChunk(args, userDetails.getUserId()));

        return Result.success(uploadService.uploadChunk(args, userId));
    }

    @PostMapping("/merge-chunks")
    public Result<MergeChunksView> mergeChunks(@RequestBody MergeChunksArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // return Result.success(uploadService.mergeChunks(args, userDetails.getUserId()));

        return Result.success(uploadService.mergeChunks(args, userId));
    }

    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody DownloadArgs args) {
        String fileName = downloadService.getDownloadFileName(args);

        StreamingResponseBody stream = downloadService.download(args);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok().headers(headers).body(stream);
    }

    @GetMapping
    public Result<List<EntryView>> listEntries(@RequestParam Long driveId, @RequestParam Long parentId) {
        List<Entry> entries = personalService.listEntries(driveId, parentId);
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
        List<FolderTreeView> views = personalService.listFolders(driveId);
        return Result.success(views);
    }

    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody CreateFolderArgs args) {
        personalService.createFolder(args, userId);
        return Result.success();
    }

    @PostMapping("/move")
    public Result<?> moveEntries(@RequestBody MoveEntryArgs args) {
        personalService.moveEntries(args, userId);
        return Result.success();
    }

    @PostMapping("/copy")
    public Result<?> copyEntry(@RequestBody CopyEntryArgs args) {
        personalService.copyEntry(args, userId);
        return Result.success();
    }

    @PostMapping("/rename")
    public Result<?> renameEntry(@RequestBody RenameEntryArgs args) {
        personalService.renameEntry(args, userId);
        return Result.success();
    }

    @GetMapping("/search")
    public Result<?> searchEntry(@RequestParam String targetName, @RequestParam Long driveId) {
        List<Entry> entries = personalService.searchEntry(targetName, driveId, userId);
        List<EntryView> views = entries.stream().map(entry -> EntryView.builder()
                .id(entry.getId())
                .name(entry.getEntryName())
                .type(entry.getEntryType())
                .size(entry.getFileSize())
                .createTime(entry.getCreatedAt())
                .build()).toList();
        return Result.success("", views);
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody DeleteEntryArgs args) {
        personalService.deleteEntries(args, userId);
        return Result.success();
    }

    @GetMapping("/usage")
    public Result<?> getPersonalDriveUsage(@RequestParam Long driveId) {
        Drive drive = driveService.getPersonalDriveUsage(driveId);
        PersonalDriveUsageView view = PersonalDriveUsageView.builder()
                .usedQuota(drive.getUsedQuota())
                .totalQuota(drive.getTotalQuota())
                .build();
        return Result.success(view);
    }

    @GetMapping("/id")
    public Result<?> getPersonalDriveId() {
        Long personalDriveId = driveService.getPersonalDriveId(userId);
        return Result.success(personalDriveId);
    }
}
