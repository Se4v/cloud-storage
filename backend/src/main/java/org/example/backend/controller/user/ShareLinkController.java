package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.DeleteLinkArgs;
import org.example.backend.model.request.UpdateLinkArgs;
import org.example.backend.model.entity.Share;
import org.example.backend.model.response.ShareView;
import org.example.backend.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/link")
public class ShareLinkController {
    @Autowired
    private ShareService shareService;

    @GetMapping
    public Result<?> listLinks() {
        Long currentUserId = SecurityUtil.getUserId();
        List<Share> shareList = shareService.listLinks(currentUserId);

        List<ShareView> views = shareList.stream()
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

        return Result.success("", views);
    }

    @PostMapping("/update")
    public Result<?> updateLink(@RequestBody UpdateLinkArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        shareService.updateLink(args, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteLinks(@RequestBody DeleteLinkArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        shareService.deleteLinks(args, currentUserId);
        return Result.success();
    }
}
