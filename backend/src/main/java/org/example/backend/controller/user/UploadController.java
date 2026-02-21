package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.InitUploadArgs;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/upload")
public class UploadController {

    @PostMapping("/init")
    public Result<?> initUpload(@RequestBody InitUploadArgs request) {

        return Result.success();
    }

    @PostMapping("/direct")
    public Result<?> directUpload() {
        return Result.success();
    }

    @PostMapping("/chunk")
    public Result<?> chunkUpload() {
        return Result.success();
    }

    @PostMapping("/merge")
    public Result<?> mergeChunk() {
        return Result.success();
    }
}
