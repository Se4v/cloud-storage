package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.CreateShareLinkArgs;
import org.example.backend.model.args.UpdateShareLinkArgs;
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
public class ShareController {
    @Autowired
    private ShareService shareService;

    @GetMapping
    public Result<List<ShareView>> listLinks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        List<Share> shareList = shareService.listLinks(userDetails.getUserId());

        List<ShareView> views = shareList.stream()
                .map(share -> {
                    ShareView shareView = new ShareView();

                    shareView.setId(String.valueOf(share.getId()));
                    shareView.setName(share.getLinkName());
                    shareView.setType(share.getEntryType() == 1 ? "file" : "folder");
                    shareView.setKey(share.getLinkKey());
                    shareView.setExpireTime(String.valueOf(share.getExpiredAt()));
                    shareView.setCreateTime(String.valueOf(share.getCreatedAt()));
                    shareView.setIsProtected(!share.getAccessCode().isEmpty());

                    return shareView;
                })
                .toList();

        return Result.success("", views);
    }

    @PostMapping("/create")
    public Result<Void> createLink(@RequestBody CreateShareLinkArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        shareService.createLink(args, userDetails.getUserId());

        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateLink(@RequestBody UpdateShareLinkArgs args) {
        shareService.updateLink(args);

        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteLinks(@RequestBody List<Long> shareIds) {
        shareService.deleteLinks(shareIds);

        return Result.success();
    }
}
