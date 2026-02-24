package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.DirectUploadArgs;
import org.example.backend.model.args.InitUploadArgs;
import org.example.backend.model.args.MergeChunksArgs;
import org.example.backend.model.args.RecordChunksArgs;
import org.example.backend.model.view.DirectUploadView;
import org.example.backend.model.view.InitUploadView;
import org.example.backend.model.view.MergeChunksView;
import org.example.backend.model.view.RecordChunksView;
import org.example.backend.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @PostMapping("/init")
    public Result<InitUploadView> initUpload(@RequestBody InitUploadArgs args,
                                             @AuthenticationPrincipal Long userId) {
        return Result.success(uploadService.initUpload(args, userId));
    }

    @PostMapping("/direct")
    public Result<DirectUploadView> directUpload(DirectUploadArgs args,
                                                 @AuthenticationPrincipal Long userId) {
        return Result.success(uploadService.directUpload(args, userId));
    }

    @PostMapping("/chunk")
    public Result<RecordChunksView> chunkUpload(@RequestBody RecordChunksArgs args,
                                                @AuthenticationPrincipal Long userId) {
        return Result.success(uploadService.recordChunks(args, userId));
    }

    @PostMapping("/merge")
    public Result<MergeChunksView> mergeChunk(@RequestBody MergeChunksArgs args,
                                              @AuthenticationPrincipal Long userId) {
        return Result.success(uploadService.mergeChunks(args.getUploadId(), userId));
    }
}
