package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.ShareMapper;
import org.example.backend.model.args.CreateShareLinkArgs;
import org.example.backend.model.args.UpdateShareLinkArgs;
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

    public List<ShareDetailResult> getAllShareLinks(Long userId) {
        return shareMapper.selectAllByUserId(userId);
    }

    @Transactional
    public void createShareLink(CreateShareLinkArgs args, Long userId) {
        Entry existEntry = entryMapper.selectById(args.getEntryId());
        if (existEntry == null) {
            throw new BusinessException("Entry does not exist");
        }

        int count = shareMapper.insert(Share.builder()
                        .driveId(args.getDriveId())
                        .entryId(existEntry.getId())
                        .entryType(existEntry.getEntryType())
                        .userId(userId)
                        .linkName(args.getLinkName())
                        .linkKey(generateLinkKey())
                        .linkType(args.getLinkType())
                        .accessCode(args.getAccessCode())
                        .deleted(0)
                        .expiredAt(args.getExpireTime())
                        .build());
        if (count != 1) {
            throw new BusinessException("Create share link failed");
        }
    }

    @Transactional
    public void updateShareLink(UpdateShareLinkArgs args) {
        Share exist = shareMapper.selectById(args.getShareId());
        if (exist == null || exist.getDeleted() == 1) {
            throw new BusinessException("Share does not exist");
        }

        Share share = Share.builder()
                .linkName(args.getLinkName())
                .linkType(args.getLinkType())
                .accessCode(args.getAccessCode())
                .deleted(args.getDeleted())
                .expiredAt(args.getExpireTime())
                .build();

        int count = shareMapper.updateByShareId(share, args.getShareId());
        if (count != 1) {
            throw new BusinessException("Update share link failed");
        }
    }

    @Transactional
    public void deleteShareLinks(List<Long> shareIds) {
        LambdaUpdateWrapper<Share> wrapper = Wrappers.<Share>lambdaUpdate();
        wrapper.in(Share::getId, shareIds)
                .set(Share::getDeleted, 1);

        int count = shareMapper.update(wrapper);
        if (count != shareIds.size()) {
            throw new BusinessException("Delete share link failed");
        }
    }

    private String generateLinkKey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
}
