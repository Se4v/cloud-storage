package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateNoticeArgs;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/notice")
public class SystemNoticeController {
    @PostMapping("/create")
    public Result<Void> createSystemNotice(@RequestBody CreateNoticeArgs args) {

        return Result.success();
    }

    public Result<Void> listSystemNotices() {
        return null;
    }
}
