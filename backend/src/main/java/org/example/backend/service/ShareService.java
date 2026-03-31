package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.model.args.ShareEntryArgs;
import org.example.backend.model.args.DeleteLinkArgs;
import org.example.backend.model.args.UpdateLinkArgs;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Share;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShareService {
    @Autowired
    private ShareMapper shareMapper;

    private static final int DELETED = 1;
    private static final int UNDELETED = 0;

    public List<Share> listLinks(Long userId) {
        // 查询分享链接列表
        LambdaQueryWrapper<Share> shareQuery = new LambdaQueryWrapper<>();
        shareQuery.eq(Share::getUserId, userId).eq(Share::getIsDeleted, UNDELETED);
        List<Share> shareList = shareMapper.selectList(shareQuery);

        if (shareList == null || shareList.isEmpty()) return List.of();

        return shareList;
    }

    @Transactional
    public void updateLink(UpdateLinkArgs args) {
        // 判断分享链接是否存在
        Share link = shareMapper.selectById(args.getId());
        if (link == null || link.getIsDeleted() == DELETED) throw new BusinessException("分享链接不存在");

        // 更新链接信息
        LambdaUpdateWrapper<Share> shareUpdate = new LambdaUpdateWrapper<>();
        shareUpdate.set(Share::getLinkName, args.getLinkName())
                .set(Share::getAccessCode, args.getAccessCode())
                .set(Share::getExpiredAt, args.getExpireTime())
                .eq(Share::getId, args.getId());
        if (args.getLinkType() == 1)
            shareUpdate.set(Share::getLinkType, args.getLinkType()).set(Share::getAccessCode, "");
        else
            shareUpdate.set(Share::getLinkType, args.getLinkType()).set(Share::getAccessCode, args.getAccessCode());

        int count = shareMapper.update(shareUpdate);
        if (count != 1) throw new BusinessException("更新分享链接信息失败");
    }

    @Transactional
    public void deleteLinks(DeleteLinkArgs args) {
        List<Long> linkIds = args.getLinkIds();

        LambdaUpdateWrapper<Share> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Share::getIsDeleted, DELETED)
                .in(Share::getId, linkIds);

        int count = shareMapper.update(wrapper);
        if (count != linkIds.size()) throw new BusinessException("删除分享链接失败");
    }
}
