package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.*;
import org.example.backend.model.view.DirectUploadView;
import org.example.backend.model.view.InitUploadView;
import org.example.backend.model.view.MergeChunksView;
import org.example.backend.model.view.RecordChunksView;
import org.example.backend.service.DownloadService;
import org.example.backend.service.DriveService;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
    @Autowired
    private UploadService uploadService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private DriveService driveService;

    @PostMapping("/init-upload")
    public Result<InitUploadView> initUpload(@RequestBody InitUploadArgs args,
                                             @AuthenticationPrincipal GlobalUserDetails userDetails) {
        return Result.success(uploadService.initUpload(args, userDetails.getUserId()));
    }

    @PostMapping("/direct-upload")
    public Result<DirectUploadView> directUpload(DirectUploadArgs args,
                                                 @AuthenticationPrincipal GlobalUserDetails userDetails) {
        return Result.success(uploadService.directUpload(args, userDetails.getUserId()));
    }

    @PostMapping("/chunk-upload")
    public Result<RecordChunksView> chunkUpload(@RequestBody RecordChunksArgs args,
                                                @AuthenticationPrincipal GlobalUserDetails userDetails) {
        return Result.success(uploadService.recordChunks(args, userDetails.getUserId()));
    }

    @PostMapping("/merge-chunk")
    public Result<MergeChunksView> mergeChunk(@RequestBody MergeChunksArgs args,
                                              @AuthenticationPrincipal GlobalUserDetails userDetails) {
        return Result.success(uploadService.mergeChunks(args.getUploadId(), userDetails.getUserId()));
    }

    @PostMapping("/download")
    public Result<?> download(@RequestBody List<Long> entryIds) {
        return null;
    }

    @GetMapping("/list")
    public Result<?> listEntries(@RequestParam(required = false) Long parentId) {
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
}
