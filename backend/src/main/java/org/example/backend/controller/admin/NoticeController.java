package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateNoticeArgs;
import org.example.backend.model.args.UpdateNoticeArgs;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.view.NoticeView;
import org.example.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @PostMapping("/create")
    public Result<Void> createNotice(@RequestBody CreateNoticeArgs args) {
        noticeService.createNotice(args);
        return Result.success();
    }

    @PostMapping("/create")
    public Result<Void> deleteNotices(@RequestBody List<Long> noticeIds) {
        noticeService.deleteNotices(noticeIds);
        return Result.success();
    }

    @PostMapping("/create")
    public Result<Void> updateNotice(@RequestBody UpdateNoticeArgs args) {
        noticeService.updateNotice(args);
        return Result.success();
    }

    @GetMapping("")
    public Result<List<NoticeView>> listAllNotices() {
        List<Notice> results = noticeService.listAllNotices();

        List<NoticeView> noticeViews = results.stream()
                .map(result -> {
                    return NoticeView.builder()
                            .noticeId(String.valueOf(result.getId()))
                            .title(result.getTitle())
                            .content(result.getContent())
                            .expiredAt(String.valueOf(result.getExpiredAt()))
                            .build();
                })
                .toList();

        return Result.success("", noticeViews);
    }
}
