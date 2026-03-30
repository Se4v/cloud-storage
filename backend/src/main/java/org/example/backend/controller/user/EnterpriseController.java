package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.OrgUserDetails;
import org.example.backend.model.args.SimpleUploadArgs;
import org.example.backend.model.args.InitUploadArgs;
import org.example.backend.model.args.MergeChunksArgs;
import org.example.backend.model.args.UploadChunkArgs;
import org.example.backend.model.view.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.OrgService;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {
    @Autowired
    private UploadService uploadService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private OrgService orgService;

    Long userId = 2034965772877197313L;

    @GetMapping("/org")
    public Result<?> getOrgTree() {
        List<OrgTreeView> views = orgService.getOrgTree(userId);
        return Result.success(views);
    }

    @PostMapping("/init-upload")
    public Result<InitUploadView> initUpload(@RequestBody InitUploadArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.initUpload(args, userDetails.getUserId()));
        return null;
    }

    @PostMapping("/simple-upload")
    public Result<SimpleUploadView> simpleUpload(SimpleUploadArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.simpleUpload(args, userDetails.getUserId()));
        return null;
    }

    @PostMapping("/upload-chunk")
    public Result<UploadChunkView> uploadChunk(@RequestBody UploadChunkArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.uploadChunk(args, userDetails.getUserId()));
        return null;
    }

    @PostMapping("/merge-chunks")
    public Result<MergeChunksView> mergeChunks(@RequestBody MergeChunksArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.mergeChunks(args, userDetails.getUserId()));
        return null;
    }

    @PostMapping("/download")
    public Result<?> download(@RequestBody List<Long> entryIds) {
        return null;
    }

    @GetMapping()
    public Result<?> listEntries(Long parentId, Long userId) {
        return Result.success();
    }

    @PostMapping("/")
    public Result<?> createEntry() {
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
}
