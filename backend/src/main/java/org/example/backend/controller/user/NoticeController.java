package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.notice.NoticeDeletionReq;
import org.example.backend.model.request.notice.NoticeReadMarkReq;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.response.notice.NoticeResp;
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
        List<Notice> notices = noticeService.listUnreadNotices();

        List<NoticeResp> resp = notices.stream()
                .map(notice -> NoticeResp.builder()
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
        List<Notice> notices = noticeService.listNotices();

        List<NoticeResp> resp = notices.stream()
                .map(notice -> NoticeResp.builder()
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
        noticeService.markNoticeAsRead(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteNotices(@RequestBody NoticeDeletionReq req) {
        noticeService.deleteNotices(req);
        return Result.success();
    }
}
