package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.request.DeleteNoticeArgs;
import org.example.backend.model.request.MarkNoticeReadArgs;
import org.example.backend.model.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    private static final int UNREAD = 0;
    private static final int UNDELETED = 0;

    public List<Notice> listUnreadNotices(Long userId) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, userId)
                .eq(Notice::getIsRead, UNREAD)
                .eq(Notice::getIsDeleted, UNDELETED);

        return noticeMapper.selectList(queryWrapper);
    }

    public List<Notice> listNotices(Long userId) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getTargetId, userId)
                .eq(Notice::getIsDeleted, UNDELETED);

        return noticeMapper.selectList(queryWrapper);
    }

    @Transactional
    public void markNoticeAsRead(MarkNoticeReadArgs args, Long userId) {
        LambdaUpdateWrapper<Notice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Notice::getIsRead, 1)
                .eq(Notice::getTargetId, userId)
                .in(Notice::getId, args.getIds());

        int count = noticeMapper.update(updateWrapper);
        if (count != args.getIds().size()) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void deleteNotices(DeleteNoticeArgs args, Long userId) {
        LambdaUpdateWrapper<Notice> noticeUpdate = new LambdaUpdateWrapper<>();
        noticeUpdate.set(Notice::getIsDeleted, 1)
                .eq(Notice::getTargetId, userId)
                .in(Notice::getId, args.getIds());
        int count = noticeMapper.update(noticeUpdate);
        if (count != args.getIds().size()) throw new BusinessException("<UNK>");
    }
}
