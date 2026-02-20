package org.example.backend.service;

import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.args.CreateNoticeArgs;
import org.example.backend.model.result.NoticeDetailResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    private static final int UNREAD = 0;

    public List<NoticeDetailResult> getUnreadNotices(Long userId) {
        return noticeMapper.selectUnreadNotices(userId);
    }

    public List<NoticeDetailResult> getAllNotices(Long userId) {
        return noticeMapper.selectAllNotices(userId);
    }

    @Transactional
    public void deleteNotices(List<Long> noticeIds) {
        int count = noticeMapper.deleteNotices(noticeIds);
        if (count != noticeIds.size()) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void markNoticeAsRead(List<Long> noticeIds) {
        int count = noticeMapper.updateReadStatus(noticeIds, 1);
        if (count != noticeIds.size()) {
            throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void createNotice(CreateNoticeArgs args) {
        Notice notice = Notice.builder()
                .receiverId(args.getReceiverId())
                .senderId(args.getSenderId())
                .title(args.getTitle())
                .content(args.getContent())
                .type(args.getType())
                .read(UNREAD)
                .createdAt(args.getSendTime())
                .build();

        int count = noticeMapper.insert(notice);
        if (count != 1) {
            throw new BusinessException("<UNK>");
        }
    }

}
