package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.*;
import org.example.backend.model.view.SimpleUploadView;
import org.example.backend.model.view.InitUploadView;
import org.example.backend.model.view.MergeChunksView;
import org.example.backend.model.view.UploadChunkView;
import org.example.backend.service.DownloadService;
import org.example.backend.service.DriveService;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Result<InitUploadView> initUpload(@RequestBody InitUploadArgs args) {
        return Result.success(uploadService.initUpload(args, userDetails.getUserId()));
    }

    @PostMapping("/simple-upload")
    public Result<SimpleUploadView> simpleUpload(@RequestBody SimpleUploadArgs args) {
        return Result.success(uploadService.simpleUpload(args, userDetails.getUserId()));
    }

    @PostMapping("/upload-chunk")
    public Result<UploadChunkView> uploadChunk(@RequestBody UploadChunkArgs args) {
        return Result.success(uploadService.uploadChunk(args, userDetails.getUserId()));
    }

    @PostMapping("/merge-chunks")
    public Result<MergeChunksView> mergeChunks(@RequestBody MergeChunksArgs args) {
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

    @GetMapping("/folder")
    public Result<?> listFolders() {
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
}
