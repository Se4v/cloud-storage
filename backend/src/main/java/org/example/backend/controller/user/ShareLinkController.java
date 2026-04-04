package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.LoginUser;
import org.example.backend.model.args.DeleteLinkArgs;
import org.example.backend.model.args.UpdateLinkArgs;
import org.example.backend.model.entity.Share;
import org.example.backend.model.view.ShareView;
import org.example.backend.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/link")
public class ShareLinkController {
    @Autowired
    private ShareService shareService;

    @GetMapping
    public Result<List<ShareView>> listLinks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) auth.getPrincipal();

        List<Share> shareList = shareService.listLinks(loginUser.getUserId());

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
    public Result<Void> updateLink(@RequestBody UpdateLinkArgs args) {
        shareService.updateLink(args);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteLinks(@RequestBody DeleteLinkArgs args) {
        shareService.deleteLinks(args);
        return Result.success();
    }
}
