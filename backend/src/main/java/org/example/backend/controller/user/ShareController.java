package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.CreateShareLinkArgs;
import org.example.backend.model.args.UpdateShareLinkArgs;
import org.example.backend.model.result.ShareDetailResult;
import org.example.backend.model.view.ShareView;
import org.example.backend.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/share")
public class ShareController {
    @Autowired
    private ShareService shareService;

    @GetMapping
    public Result<List<ShareView>> getAllShareLinks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        List<ShareDetailResult> results = shareService.getAllShareLinks(userDetails.getUserId());

        List<ShareView> views = results.stream()
                .map(result -> {
                    return ShareView.builder()
                            .shareId(result.getShareId().toString())
                            .driveId(result.getDriveId().toString())
                            .driveName(result.getDriveName())
                            .shareLinkName(result.getShareLinkName())
                            .shareLinkKey(result.getShareLinkKey())
                            .expireTime(result.getExpiredAt().toString())
                            .createTime(result.getCreatedAt().toString())
                            .build();
                })
                .toList();

        return Result.success("", views);
    }

    @PostMapping("/create")
    public Result<Void> createShareLink(@RequestBody CreateShareLinkArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        shareService.createShareLink(args, userDetails.getUserId());

        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateShareLink(@RequestBody UpdateShareLinkArgs args) {
        shareService.updateShareLink(args);

        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteShareLinks(@RequestBody List<Long> shareIds) {
        shareService.deleteShareLinks(shareIds);

        return Result.success();
    }
}
