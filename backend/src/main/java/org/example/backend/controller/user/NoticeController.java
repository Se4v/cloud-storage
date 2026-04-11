package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.notice.NoticeDeletionReq;
import org.example.backend.model.request.notice.NoticeReadMarkReq;
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

        List<NoticeView> resp = notices.stream()
                .map(notice -> NoticeView.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .content(notice.getContent())
                        .type(notice.getType())
                        .isRead(notice.getIsRead() == 1)
                        .createTime(notice.getCreatedAt())
                        .build())
                .toList();

        return Result.success(resp);
    }

    @GetMapping
    public Result<?> listNotices() {
        Long currentUserId = SecurityUtil.getUserId();
        List<Notice> notices = noticeService.listNotices(currentUserId);

        List<NoticeView> resp = notices.stream()
                .map(notice -> NoticeView.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .content(notice.getContent())
                        .type(notice.getType())
                        .isRead(notice.getIsRead() == 1)
                        .createTime(notice.getCreatedAt())
                        .build())
                .toList();

        return Result.success(resp);
    }

    @PostMapping("/read")
    public Result<?> markNoticeAsRead(@RequestBody NoticeReadMarkReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        noticeService.markNoticeAsRead(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteNotices(@RequestBody NoticeDeletionReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        noticeService.deleteNotices(req, currentUserId);
        return Result.success();
    }
}
