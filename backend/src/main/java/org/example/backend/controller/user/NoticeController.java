package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.MyUserDetails;
import org.example.backend.model.args.CreateNoticeArgs;
import org.example.backend.model.result.NoticeDetailResult;
import org.example.backend.model.view.NoticeView;
import org.example.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @PostMapping("/unread")
    public Result<List<NoticeView>> getUnreadNotices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        List<NoticeDetailResult> results = noticeService.getUnreadNotices(userDetails.getUserId());

        List<NoticeView> noticeViews = results.stream()
                .map(result -> {
                    return NoticeView.builder()
                            .noticeId(String.valueOf(result.getId()))
                            .senderName(result.getSenderName())
                            .title(result.getTitle())
                            .content(result.getContent())
                            .sendTime(String.valueOf(result.getCreatedAt()))
                            .build();
                })
                .toList();

        return Result.success("", noticeViews);
    }

    @PostMapping("/all")
    public Result<?> getAllNotices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        List<NoticeDetailResult> results = noticeService.getAllNotices(userDetails.getUserId());

        List<NoticeView> noticeViews = results.stream()
                .map(result -> {
                    return NoticeView.builder()
                            .noticeId(String.valueOf(result.getId()))
                            .senderName(result.getSenderName())
                            .title(result.getTitle())
                            .content(result.getContent())
                            .sendTime(String.valueOf(result.getCreatedAt()))
                            .build();
                })
                .toList();

        return Result.success("", noticeViews);
    }

    @PostMapping("/delete")
    public Result<Void> deleteNotices(@RequestBody List<Long> noticeIds) {
        noticeService.deleteNotices(noticeIds);

        return Result.success();
    }

    @PostMapping("/read")
    public Result<Void> markNoticeAsRead(@RequestBody List<Long> noticeIds) {
        noticeService.markNoticeAsRead(noticeIds);

        return Result.success();
    }

    @PostMapping("/create")
    public Result<Void> createNotice(@RequestBody CreateNoticeArgs args) {
        noticeService.createNotice(args);

        return Result.success();
    }

}
