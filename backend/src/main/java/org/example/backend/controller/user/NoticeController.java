package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.CreateNoticeArgs;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.view.NoticeView;
import org.example.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @PostMapping("/unread")
    public Result<List<NoticeView>> listUnreadAlerts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        List<Notice> results = noticeService.listUnreadAlerts(userDetails.getUserId());

        List<NoticeView> noticeViews = results.stream()
                .map(result -> NoticeView.builder()
                        .noticeId(String.valueOf(result.getId()))
                        .title(result.getTitle())
                        .content(result.getContent())
                        .createdAt(String.valueOf(result.getCreatedAt()))
                        .build())
                .toList();

        return Result.success("", noticeViews);
    }

    @GetMapping("")
    public Result<?> listNotices() {
        List<Notice> results = noticeService.listNotices();

        List<NoticeView> noticeViews = results.stream()
                .map(result -> NoticeView.builder()
                        .noticeId(String.valueOf(result.getId()))
                        .title(result.getTitle())
                        .content(result.getContent())
                        .createdAt(String.valueOf(result.getCreatedAt()))
                        .build())
                .toList();

        return Result.success("", noticeViews);
    }

    @PostMapping("/read")
    public Result<Void> markAlertAsRead(@RequestBody List<Long> noticeIds) {
        noticeService.markAlertAsRead(noticeIds);

        return Result.success();
    }
}
