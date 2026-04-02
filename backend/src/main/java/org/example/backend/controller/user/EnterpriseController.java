package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.*;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.view.*;
import org.example.backend.service.DownloadService;
import org.example.backend.service.EnterpriseService;
import org.example.backend.service.OrgService;
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
        return Result.success(uploadService.initUpload(args, userId));
    }

    @PostMapping("/simple-upload")
    public Result<SimpleUploadView> simpleUpload(@RequestBody SimpleUploadArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.simpleUpload(args, userDetails.getUserId()));
        return Result.success(uploadService.simpleUpload(args, userId));
    }

    @PostMapping("/upload-chunk")
    public Result<UploadChunkView> uploadChunk(@RequestBody UploadChunkArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.uploadChunk(args, userDetails.getUserId()));
        return Result.success(uploadService.uploadChunk(args, userId));
    }

    @PostMapping("/merge-chunks")
    public Result<MergeChunksView> mergeChunks(@RequestBody MergeChunksArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // OrgUserDetails userDetails = (OrgUserDetails) auth.getPrincipal();
        //
        // return Result.success(uploadService.mergeChunks(args, userDetails.getUserId()));
        return Result.success(uploadService.mergeChunks(args, userId));
    }

    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody DownloadArgs args) {
        String fileName = downloadService.getDownloadFileName(args);

        StreamingResponseBody stream = downloadService.download(args, userId);

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
        List<Entry> entries = enterpriseService.listEntries(driveId, parentId);
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
        List<FolderTreeView> views = enterpriseService.listFolders(driveId);
        return Result.success(views);
    }

    @PostMapping("/create")
    public Result<?> createFolder(@RequestBody CreateFolderArgs args) {
        enterpriseService.createFolder(args, userId);
        return Result.success();
    }

    @PostMapping("/move")
    public Result<?> moveEntries(@RequestBody MoveEntryArgs args) {
        enterpriseService.moveEntries(args, userId);
        return Result.success();
    }

    @PostMapping("/copy")
    public Result<?> copyEntry(@RequestBody CopyEntryArgs args) {
        enterpriseService.copyEntry(args, userId);
        return Result.success();
    }

    @PostMapping("/rename")
    public Result<?> renameEntry(@RequestBody RenameEntryArgs args) {
        enterpriseService.renameEntry(args, userId);
        return Result.success();
    }

    @PostMapping("/search")
    public Result<?> searchEntry(@RequestParam String targetName, @RequestParam Long driveId) {
        List<Entry> entries = enterpriseService.searchEntry(targetName, driveId, userId);
        List<EntryView> views = entries.stream().map(entry -> EntryView.builder()
                .id(entry.getId())
                .name(entry.getEntryName())
                .type(entry.getEntryType())
                .size(entry.getFileSize())
                .createTime(entry.getCreatedAt())
                .build()).toList();
        return Result.success("", views);
    }

    @PostMapping("/share")
    public Result<?> shareEntry(@RequestBody ShareEntryArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // shareService.createLink(args, userDetails.getUserId());
        enterpriseService.shareEntry(args, userId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody DeleteEntryArgs args) {
        enterpriseService.deleteEntries(args, userId);
        return Result.success();
    }
}
