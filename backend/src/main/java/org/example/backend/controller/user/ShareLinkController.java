package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.request.file.EntryDownloadReq;
import org.example.backend.model.request.share.LinkCheckReq;
import org.example.backend.model.request.share.LinkDeletionReq;
import org.example.backend.model.request.share.LinkUpdateReq;
import org.example.backend.model.entity.Share;
import org.example.backend.model.response.file.EntryResp;
import org.example.backend.model.response.share.LinkInfoResp;
import org.example.backend.model.response.share.ShareLinkResp;
import org.example.backend.service.DownloadService;
import org.example.backend.service.ShareService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/share")
public class ShareLinkController {
    private final ShareService shareService;
    private final DownloadService downloadService;

    public ShareLinkController(ShareService shareService, DownloadService downloadService) {
        this.shareService = shareService;
        this.downloadService = downloadService;
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

    /**
     * 获取分享文件列表
     * @param linkKey 分享链接唯一标识
     * @param parentId 父目录ID
     * @return 统一响应结果
     */
    @GetMapping("/file")
    public Result<?> listEntries(@RequestParam(required = false) String linkKey,
                                 @RequestParam(required = false) Long parentId) {
        List<Entry> entries = shareService.listEntries(linkKey, parentId);
        List<EntryResp> resp = entries.stream()
                .map(entry -> EntryResp.builder()
                        .id(entry.getId())
                        .name(entry.getEntryName())
                        .type(entry.getEntryType())
                        .size(entry.getFileSize())
                        .createTime(entry.getCreatedAt())
                        .build())
                .toList();
        return Result.success(resp);
    }

    /**
     * 获取分享信息
     * @param linkKey 分享链接唯一标识
     * @return 统一响应结果
     */
    @GetMapping("/info")
    public Result<?> getLinkInfo(@RequestParam String linkKey) {
        LinkInfoResp resp = shareService.getLinkInfo(linkKey);
        return Result.success(resp);
    }

    /**
     * 文件下载
     * @param req 文件下载请求参数
     * @return 文件流响应实体
     */
    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody EntryDownloadReq req) {
        String fileName = downloadService.getDownloadFileName(req);

        StreamingResponseBody stream = downloadService.download(req, false);

        HttpHeaders headers = new HttpHeaders();
        if (req.getIds().size() == 1) {
            String mimeType = downloadService.getFileContentType(req);
            headers.setContentType(MediaType.parseMediaType(mimeType));
        } else if (req.getIds().size() > 1) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok().headers(headers).body(stream);
    }

    /**
     * 校验提取码
     * @param req 校验提取码请求参数
     * @return 统一响应结果
     */
    @PostMapping("/check")
    public Result<?> checkAccessCode(@RequestBody LinkCheckReq req) {
        shareService.checkAccessCode(req);
        return Result.success();
    }
}
