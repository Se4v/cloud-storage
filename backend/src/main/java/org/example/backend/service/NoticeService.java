package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.request.DeleteNoticeArgs;
import org.example.backend.model.request.MarkNoticeReadArgs;
import org.example.backend.model.entity.Notice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService {
    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    private static final int UNREAD = 0;
    private static final int UNDELETED = 0;

    public List<Notice> listUnreadNotices(Long userId) {
        List<Notice> unreadNotices = noticeMapper.selectList(
                Wrappers.<Notice>lambdaQuery()
                        .eq(Notice::getTargetId, userId)
                        .eq(Notice::getIsRead, UNREAD)
                        .eq(Notice::getIsDeleted, UNDELETED));

        return unreadNotices;
    }

    public List<Notice> listNotices(Long userId) {
        List<Notice> notices = noticeMapper.selectList(
                Wrappers.<Notice>lambdaQuery()
                        .eq(Notice::getTargetId, userId)
                        .eq(Notice::getIsDeleted, UNDELETED));

        return notices;
    }

    @Transactional
    public void markNoticeAsRead(MarkNoticeReadArgs args, Long userId) {
        int count = noticeMapper.update(
                Wrappers.<Notice>lambdaUpdate()
                        .set(Notice::getIsRead, 1)
                        .eq(Notice::getTargetId, userId)
                        .in(Notice::getId, args.getIds()));
        if (count != args.getIds().size()) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void deleteNotices(DeleteNoticeArgs args, Long userId) {
        int count = noticeMapper.update(
                Wrappers.<Notice>lambdaUpdate()
                        .set(Notice::getIsDeleted, 1)
                        .eq(Notice::getTargetId, userId)
                        .in(Notice::getId, args.getIds()));
        if (count != args.getIds().size()) throw new BusinessException("<UNK>");
    }
}
