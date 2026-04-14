package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.request.share.LinkDeletionReq;
import org.example.backend.model.request.share.LinkUpdateReq;
import org.example.backend.model.entity.Share;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShareService {
    private final ShareMapper shareMapper;
    private final EntryMapper entryMapper;

    public ShareService(ShareMapper shareMapper, EntryMapper entryMapper) {
        this.shareMapper = shareMapper;
        this.entryMapper = entryMapper;
    }

    /**
     * 列出当前用户的分享链接
     * @return 分享链接列表
     */
    public List<Share> listLinks() {
        // 查询分享链接列表
        Long currentUserId = SecurityUtils.getUserId();
        List<Share> shareList = shareMapper.selectList(
                Wrappers.<Share>lambdaQuery()
                        .eq(Share::getUserId, currentUserId)
                        .eq(Share::getIsDeleted, DbConsts.DELETED_NO));

        if (shareList == null || shareList.isEmpty()) return List.of();

        return shareList;
    }

    public List<Entry> listEntries(String linkKey, Long parentId) {
        if (linkKey != null) {
            Share link = shareMapper.selectOne(
                    Wrappers.<Share>lambdaQuery()
                            .eq(Share::getLinkKey, linkKey)
                            .eq(Share::getIsDeleted, DbConsts.DELETED_NO)
                            .gt(Share::getExpiredAt, LocalDateTime.now()));
            if (link == null) return List.of();
            List<Entry> entries = entryMapper.selectList(
                    Wrappers.<Entry>lambdaQuery()
                            .eq(Entry::getId, link.getEntryId())
                            .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
            return entries;
        }
        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery()
                        .eq(Entry::getParentId, parentId)
                        .eq(Entry::getStatus, DbConsts.ENTRY_STATUS_UNDELETED));
        if (entries == null || entries.isEmpty()) return List.of();

        return entries;
    }

    /**
     * 更新分享链接
     * @param req 链接更新请求
     */
    @Transactional
    public void updateLink(LinkUpdateReq req) {
        // 判断分享链接是否存在
        Long currentUserId = SecurityUtils.getUserId();
        Share link = shareMapper.selectOne(
                Wrappers.<Share>lambdaQuery()
                        .eq(Share::getUserId, currentUserId)
                        .eq(Share::getIsDeleted, DbConsts.DELETED_NO));
        if (link == null) throw new BusinessException("分享链接不存在");

        // 更新链接信息
        String accessCode = req.getLinkType() == 1 ? "" : req.getAccessCode();
        int count = shareMapper.update(
                Wrappers.<Share>lambdaUpdate()
                        .set(Share::getLinkName, req.getLinkName())
                        .set(Share::getAccessCode, accessCode)
                        .set(Share::getExpiredAt, req.getExpireTime())
                        .set(Share::getLinkType, req.getLinkType())
                        .eq(Share::getUserId, currentUserId)
                        .eq(Share::getId, req.getId()));
        if (count != 1) throw new BusinessException("更新分享链接信息失败");
    }

    /**
     * 删除分享链接
     * @param req 链接删除请求
     */
    @Transactional
    public void deleteLinks(LinkDeletionReq req) {
        Long currentUserId = SecurityUtils.getUserId();

        int count = shareMapper.update(
                Wrappers.<Share>lambdaUpdate()
                        .set(Share::getIsDeleted, DbConsts.DELETED_YES)
                        .eq(Share::getUserId, currentUserId)
                        .in(Share::getId, req.getLinkIds()));
        if (count != req.getLinkIds().size()) throw new BusinessException("删除分享链接失败");
    }
}
