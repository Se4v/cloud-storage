package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateLinkArgs;
import org.example.backend.model.args.DeleteLinkArgs;
import org.example.backend.model.args.UpdateLinkArgs;
import org.example.backend.model.entity.Share;
import org.example.backend.model.view.ShareView;
import org.example.backend.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/link")
public class ShareController {
    @Autowired
    private ShareService shareService;

    Long userId = 2034965772877197313L;

    @GetMapping
    public Result<List<ShareView>> listLinks() {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        // List<Share> shareList = shareService.listLinks(userDetails.getUserId());

        List<Share> shareList = shareService.listLinks(userId);

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

    @PostMapping("/create")
    public Result<Void> createLink(@RequestBody CreateLinkArgs args) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // shareService.createLink(args, userDetails.getUserId());
        shareService.createLink(args, userId);
        return Result.success();
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
