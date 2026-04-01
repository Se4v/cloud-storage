package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.MarkNoticeReadArgs;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.view.NoticeView;
import org.example.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/unread")
    public Result<List<NoticeView>> listUnreadNotices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        List<Notice> results = noticeService.listUnreadAlerts(userDetails.getUserId());

        List<NoticeView> noticeViews = results.stream()
                .map(result -> NoticeView.builder()
                        .id(result.getId())
                        .title(result.getTitle())
                        .content(result.getContent())
                        .createdTime(result.getCreatedAt().format(formatter))
                        .build())
                .toList();

        return Result.success("", noticeViews);
    }

    @GetMapping
    public Result<?> listNotices() {
        List<Notice> results = noticeService.listNotices();

        List<NoticeView> noticeViews = results.stream()
                .map(result -> NoticeView.builder()
                        .id(result.getId())
                        .title(result.getTitle())
                        .content(result.getContent())
                        .createdTime(result.getCreatedAt().format(formatter))
                        .build())
                .toList();

        return Result.success("", noticeViews);
    }

    @PostMapping("/read")
    public Result<Void> markNoticeAsRead(@RequestBody MarkNoticeReadArgs args) {
        noticeService.markAlertAsRead(args.getNoticeIds());

        return Result.success();
    }
}
