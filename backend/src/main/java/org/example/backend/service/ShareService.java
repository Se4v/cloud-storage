package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.model.request.DeleteLinkArgs;
import org.example.backend.model.request.UpdateLinkArgs;
import org.example.backend.model.entity.Share;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShareService {
    @Autowired
    private ShareMapper shareMapper;

    private static final int DELETED = 1;
    private static final int UNDELETED = 0;

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
    public void updateLink(UpdateLinkArgs args, Long userId) {
        // 判断分享链接是否存在
        Share link = shareMapper.selectById(args.getId());
        if (link == null || link.getIsDeleted() == DELETED) throw new BusinessException("分享链接不存在");

        // 更新链接信息
        LambdaUpdateWrapper<Share> shareUpdate = new LambdaUpdateWrapper<>();
        shareUpdate.set(Share::getLinkName, args.getLinkName())
                .set(Share::getAccessCode, args.getAccessCode())
                .set(Share::getExpiredAt, args.getExpireTime())
                .eq(Share::getUserId, userId)
                .eq(Share::getId, args.getId());
        if (args.getLinkType() == 1)
            shareUpdate.set(Share::getLinkType, args.getLinkType()).set(Share::getAccessCode, "");
        else
            shareUpdate.set(Share::getLinkType, args.getLinkType()).set(Share::getAccessCode, args.getAccessCode());

        int count = shareMapper.update(shareUpdate);
        if (count != 1) throw new BusinessException("更新分享链接信息失败");
    }

    @Transactional
    public void deleteLinks(DeleteLinkArgs args, Long userId) {
        List<Long> linkIds = args.getLinkIds();

        int count = shareMapper.update(
                Wrappers.<Share>lambdaUpdate()
                        .set(Share::getIsDeleted, DELETED)
                        .eq(Share::getUserId, userId)
                        .in(Share::getId, linkIds));
        if (count != linkIds.size()) throw new BusinessException("删除分享链接失败");
    }
}
