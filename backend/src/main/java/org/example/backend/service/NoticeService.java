package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.Result;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.args.UpdateNoticeArgs;
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

    private static final int UNREAD = 0;
    private static final int UNDELETED = 0;
    private static final int DELETED = 1;
    private static final int NOTICE = 1;
    private static final int ALERT = 2;
    private static final int ALL_USER = 0;

    public List<Notice> listUnreadAlerts(Long userId) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, userId)
                .eq(Notice::getRead, UNREAD);

        return noticeMapper.selectList(queryWrapper);
    }

    public List<Notice> listNotices() {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, ALL_USER)
                .eq(Notice::getType, NOTICE)
                .eq(Notice::getDeleted, UNDELETED)
                .gt(Notice::getExpiredAt, LocalDateTime.now());

        return noticeMapper.selectList(queryWrapper);
    }

    @Transactional
    public void markAlertAsRead(List<Long> noticeIds) {
        LambdaUpdateWrapper<Notice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notice::getType, ALERT)
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
                .type(NOTICE)
                .expiredAt(LocalDateTime.now().plusDays(15))
                .createdAt(LocalDateTime.now())
                .build();

        int count = noticeMapper.insert(notice);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void deleteNotices(List<Long> noticeIds) {
        LambdaUpdateWrapper<Notice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Notice::getId, noticeIds)
                .set(Notice::getDeleted, DELETED);
        int count = noticeMapper.update(updateWrapper);
        if (count != noticeIds.size()) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void updateNotice(UpdateNoticeArgs args) {
        LambdaUpdateWrapper<Notice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getTitle() != null, Notice::getTitle, args.getTitle())
                .set(args.getContent() != null, Notice::getContent, args.getContent());
        if (args.getExpiredAt() != null) {
            updateWrapper.set( Notice::getExpiredAt, LocalDateTime.parse(args.getExpiredAt()));
        }
        updateWrapper.eq(Notice::getId, args.getId());

        int count = noticeMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

    public List<Notice> listAllNotices() {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, ALL_USER)
                .eq(Notice::getType, NOTICE)
                .eq(Notice::getDeleted, UNDELETED);

        return noticeMapper.selectList(queryWrapper);
    }
}
