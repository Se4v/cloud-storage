package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.share.LinkDeletionReq;
import org.example.backend.model.request.share.LinkUpdateReq;
import org.example.backend.model.entity.Share;
import org.example.backend.model.response.share.ShareLinkResp;
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
        List<Share> shareList = shareService.listLinks();

        List<ShareLinkResp> resp = shareList.stream()
                .map(share -> ShareLinkResp.builder()
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
        shareService.updateLink(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteLinks(@RequestBody LinkDeletionReq req) {
        shareService.deleteLinks(req);
        return Result.success();
    }
}
