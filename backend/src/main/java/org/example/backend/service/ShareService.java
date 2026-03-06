package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.model.args.CreateShareLinkArgs;
import org.example.backend.model.args.UpdateShareLinkArgs;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Share;
import org.example.backend.model.result.ShareDetailResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShareService {
    @Autowired
    private ShareMapper shareMapper;
    @Autowired
    private EntryMapper entryMapper;

    private static final int DELETED = 1;
    private static final int UNDELETED = 0;

    public List<ShareDetailResult> listLinks(Long userId) {
        // 查询分享链接列表
        LambdaQueryWrapper<Share> shareQuery = new LambdaQueryWrapper<>();
        shareQuery.eq(Share::getUserId, userId);
        List<Share> shareList = shareMapper.selectList(shareQuery);

        if (shareList == null || shareList.isEmpty()) return List.of();

        // 合并数据
        List<ShareDetailResult> results = shareList.stream()
                .map(share -> {
                    ShareDetailResult result = new ShareDetailResult();
                    result.setShareId(share.getId());
                    result.setLinkName(share.getLinkName());
                    result.setLinkKey(share.getLinkKey());
                    result.setExpiredAt(share.getExpiredAt());
                    result.setCreatedAt(share.getCreatedAt());

                    Drive drive = driveMap.get(share.getDriveId());
                    if (drive != null) {
                        result.setDriveName(drive.getDriveName());
                    } else {
                        result.setDriveName("未知");
                    }

                    return result;
                })
                .toList();

        return results;
    }

    @Transactional
    public void createLink(CreateShareLinkArgs args, Long userId) {
        // 判断文件条目是否存在
        Entry entry = entryMapper.selectById(args.getEntryId());
        if (entry == null) throw new BusinessException("文件条目不存在");

        int count = shareMapper.insert(Share.builder()
                        .driveId(args.getDriveId())
                        .entryId(entry.getId())
                        .entryType(entry.getEntryType())
                        .userId(userId)
                        .linkName(args.getLinkName())
                        .linkKey(generateLinkKey())
                        .linkType(args.getLinkType())
                        .accessCode(args.getAccessCode())
                        .deleted(UNDELETED)
                        .expiredAt(args.getExpireTime())
                        .build());

        if (count != 1) throw new BusinessException("Create share link failed");
    }

    @Transactional
    public void updateLink(UpdateShareLinkArgs args) {
        // 判断分享链接是否存在
        Share link = shareMapper.selectById(args.getShareId());
        if (link == null || link.getDeleted() == DELETED) throw new BusinessException("分享链接不存在");

        LambdaUpdateWrapper<Share> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getLinkName() != null, Share::getLinkName, args.getLinkName())
                .set(args.getAccessCode() != null, Share::getAccessCode, args.getAccessCode())
                .set(args.getLinkType() != null, Share::getLinkType, args.getLinkType())
                .set(args.getExpiredAt() != null, Share::getExpiredAt, args.getExpiredAt())
                .eq(Share::getId, args.getShareId());

        int count = shareMapper.update(updateWrapper);
        if (count != 1) throw new BusinessException("更新分享链接信息失败");
    }

    @Transactional
    public void deleteLinks(List<Long> shareIds) {
        LambdaUpdateWrapper<Share> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Share::getDeleted, DELETED)
                .in(Share::getId, shareIds);

        int count = shareMapper.update(wrapper);
        if (count != shareIds.size()) throw new BusinessException("删除分享链接失败");
    }

    private String generateLinkKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
}
