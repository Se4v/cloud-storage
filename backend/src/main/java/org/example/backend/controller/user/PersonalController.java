package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.*;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.view.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.DriveService;
import org.example.backend.service.PersonalService;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public Result<?> download(@RequestBody List<Long> entryIds) {
        return null;
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
        return Result.success();
    }

    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody CreateFolderArgs args) {
        return Result.success();
    }

    @PostMapping("/move")
    public Result<?> moveEntries() {
        return Result.success();
    }

    @PostMapping("/copy")
    public Result<?> copyEntries() {
        return Result.success();
    }

    @PostMapping("/rename")
    public Result<?> renameEntry() {
        return Result.success();
    }

    @PostMapping("/search")
    public Result<?> searchEntry() {
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries() {
        return Result.success();
    }

    @GetMapping("/usage")
    public Result<?> getPersonalDriveUsage() {
        return null;
    }

    @GetMapping("/id")
    public Result<?> getPersonalDriveId() {
        Long personalDriveId = personalService.getPersonalDriveId(userId);
        return Result.success(personalDriveId);
    }
}
