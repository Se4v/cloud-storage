package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.model.request.share.LinkDeletionReq;
import org.example.backend.model.request.share.LinkUpdateReq;
import org.example.backend.model.entity.Share;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShareService {
    private final ShareMapper shareMapper;

    private static final int DELETED = 1;
    private static final int UNDELETED = 0;

    public ShareService(ShareMapper shareMapper) {
        this.shareMapper = shareMapper;
    }

    public List<Share> listLinks(Long userId) {
        // 查询分享链接列表
        List<Share> shareList = shareMapper.selectList(
                Wrappers.<Share>lambdaQuery()
                        .eq(Share::getUserId, userId)
                        .eq(Share::getIsDeleted, UNDELETED));

        if (shareList == null || shareList.isEmpty()) return List.of();

        return shareList;
    }

    @Transactional
    public void updateLink(LinkUpdateReq req, Long userId) {
        // 判断分享链接是否存在
        Share link = shareMapper.selectOne(
                Wrappers.<Share>lambdaQuery()
                        .eq(Share::getUserId, userId)
                        .eq(Share::getIsDeleted, UNDELETED));
        if (link == null) throw new BusinessException("分享链接不存在");

        // 更新链接信息
        String accessCode = req.getLinkType() == 1 ? "" : req.getAccessCode();
        int count = shareMapper.update(
                Wrappers.<Share>lambdaUpdate()
                        .set(Share::getLinkName, req.getLinkName())
                        .set(Share::getAccessCode, accessCode)
                        .set(Share::getExpiredAt, req.getExpireTime())
                        .set(Share::getLinkType, req.getLinkType())
                        .eq(Share::getUserId, userId)
                        .eq(Share::getId, req.getId()));
        if (count != 1) throw new BusinessException("更新分享链接信息失败");
    }

    @Transactional
    public void deleteLinks(LinkDeletionReq req, Long userId) {
        List<Long> linkIds = req.getLinkIds();

        int count = shareMapper.update(
                Wrappers.<Share>lambdaUpdate()
                        .set(Share::getIsDeleted, DELETED)
                        .eq(Share::getUserId, userId)
                        .in(Share::getId, linkIds));
        if (count != linkIds.size()) throw new BusinessException("删除分享链接失败");
    }
}
