package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.DeleteNoticeArgs;
import org.example.backend.model.request.MarkNoticeReadArgs;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.response.NoticeView;
import org.example.backend.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/unread")
    public Result<?> listUnreadNotices() {
        Long currentUserId = SecurityUtil.getUserId();
        List<Notice> notices = noticeService.listUnreadNotices(currentUserId);

        List<NoticeView> noticeViews = notices.stream()
                .map(notice -> NoticeView.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .content(notice.getContent())
                        .type(notice.getType())
                        .isRead(notice.getIsRead() == 1)
                        .createTime(notice.getCreatedAt())
                        .build())
                .toList();

        return Result.success("", noticeViews);
    }

    @GetMapping
    public Result<?> listNotices() {
        Long currentUserId = SecurityUtil.getUserId();
        List<Notice> notices = noticeService.listNotices(currentUserId);

        List<NoticeView> noticeViews = notices.stream()
                .map(notice -> NoticeView.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .content(notice.getContent())
                        .type(notice.getType())
                        .isRead(notice.getIsRead() == 1)
                        .createTime(notice.getCreatedAt())
                        .build())
                .toList();

        return Result.success("", noticeViews);
    }

    @PostMapping("/read")
    public Result<?> markNoticeAsRead(@RequestBody MarkNoticeReadArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        noticeService.markNoticeAsRead(args, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteNotices(@RequestBody DeleteNoticeArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        noticeService.deleteNotices(args, currentUserId);
        return Result.success();
    }
}
