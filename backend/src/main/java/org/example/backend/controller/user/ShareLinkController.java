package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.share.LinkDeletionReq;
import org.example.backend.model.request.share.LinkUpdateReq;
import org.example.backend.model.entity.Share;
import org.example.backend.model.response.ShareView;
import org.example.backend.service.ShareService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/link")
public class ShareLinkController {
    private final ShareService shareService;

    public ShareLinkController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping
    public Result<?> listLinks() {
        Long currentUserId = SecurityUtil.getUserId();
        List<Share> shareList = shareService.listLinks(currentUserId);

        List<ShareView> resp = shareList.stream()
                .map(share -> ShareView.builder()
                        .id(share.getId())
                        .fileType(share.getEntryType())
                        .linkName(share.getLinkName())
                        .linkKey(share.getLinkKey())
                        .linkType(share.getLinkType())
                        .accessCode(share.getAccessCode())
                        .expireTime(share.getExpiredAt())
                        .createTime(share.getCreatedAt())
                        .build())
                .toList();

        return Result.success(resp);
    }

    @PostMapping("/update")
    public Result<?> updateLink(@RequestBody LinkUpdateReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        shareService.updateLink(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteLinks(@RequestBody LinkDeletionReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        shareService.deleteLinks(req, currentUserId);
        return Result.success();
    }
}
