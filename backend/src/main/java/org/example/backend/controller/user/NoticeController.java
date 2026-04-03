package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.DeleteNoticeArgs;
import org.example.backend.model.args.MarkNoticeReadArgs;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.view.NoticeView;
import org.example.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    Long userId = 2034965772877197313L;

    @GetMapping("/unread")
    public Result<List<NoticeView>> listUnreadNotices() {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        // List<Notice> notices = noticeService.listUnreadNotices(userDetails.getUserId());

        List<Notice> notices = noticeService.listUnreadNotices(userId);

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
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        //
        // List<Notice> notices = noticeService.listNotices(userDetails.getUserId());

        List<Notice> notices = noticeService.listNotices(userId);

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
    public Result<Void> markNoticeAsRead(@RequestBody MarkNoticeReadArgs args) {
        noticeService.markNoticeAsRead(args);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteNotices(@RequestBody DeleteNoticeArgs args) {
        noticeService.deleteNotices(args);
        return Result.success();
    }
}
