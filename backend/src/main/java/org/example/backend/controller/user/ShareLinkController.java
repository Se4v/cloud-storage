package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.request.share.LinkDeletionReq;
import org.example.backend.model.request.share.LinkUpdateReq;
import org.example.backend.model.entity.Share;
import org.example.backend.model.response.file.EntryResp;
import org.example.backend.model.response.share.ShareLinkResp;
import org.example.backend.service.ShareService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/share")
public class ShareLinkController {
    private final ShareService shareService;

    public ShareLinkController(ShareService shareService) {
        this.shareService = shareService;
    }

    /**
     * 查询分享链接列表
     * @return 分享链接列表
     */
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

    @GetMapping("/file")
    public Result<?> listEntries(@RequestParam(required = false) String linkKey,
                                 @RequestParam(required = false) Long parentId) {
        List<Entry> entries = shareService.listEntries(linkKey, parentId);
        List<EntryResp> resp = entries.stream().map(entry -> EntryResp.builder()
                .id(entry.getId())
                .name(entry.getEntryName())
                .type(entry.getEntryType())
                .size(entry.getFileSize())
                .createTime(entry.getCreatedAt())
                .build()).toList();
        return Result.success(resp);
    }

    /**
     * 更新分享链接信息
     * @param req 分享链接更新请求参数
     * @return 统一响应结果
     */
    @PostMapping("/update")
    public Result<?> updateLink(@RequestBody LinkUpdateReq req) {
        shareService.updateLink(req);
        return Result.success();
    }

    /**
     * 批量删除分享链接
     * @param req 分享链接删除请求参数
     * @return 统一响应结果
     */
    @PostMapping("/delete")
    public Result<?> deleteLinks(@RequestBody LinkDeletionReq req) {
        shareService.deleteLinks(req);
        return Result.success();
    }
}
