package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.args.CreateNoticeArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    private static final int UNREAD = 1;
    private static final int STATELESS = 3;
    private static final int ALL_USER = 0;

    public List<Notice> getUnreadAlerts(Long userId) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, userId)
                .eq(Notice::getStatus, UNREAD);

        return noticeMapper.selectList(queryWrapper);
    }

    public List<Notice> getNotices() {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, ALL_USER)
                .eq(Notice::getStatus, STATELESS)
                .gt(Notice::getExpiredAt, LocalDateTime.now());

        return noticeMapper.selectList(queryWrapper);
    }

    @Transactional
    public void markAlertAsRead(List<Long> noticeIds) {
        LambdaUpdateWrapper<Notice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notice::getStatus, STATELESS)
                .in(Notice::getId, noticeIds);

        int count = noticeMapper.update(updateWrapper);
        if (count != noticeIds.size()) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void createNotice(CreateNoticeArgs args) {
        Notice notice = Notice.builder()
                .title(args.getTitle())
                .content(args.getContent())
                .type(args.getType())
                .targetId(args.getTargetId())
                .status(STATELESS)
                .expiredAt(LocalDateTime.now().plusDays(15))
                .createdAt(LocalDateTime.now())
                .build();

        int count = noticeMapper.insert(notice);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

}
