package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.request.notice.NoticeDeletionReq;
import org.example.backend.model.request.notice.NoticeReadMarkReq;
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

    /**
     * 列出当前用户的未读通知
     * @return 未读通知列表
     */
    public List<Notice> listUnreadNotices() {
        Long currentUserId = SecurityUtils.getUserId();
        List<Notice> unreadNotices = noticeMapper.selectList(
                Wrappers.<Notice>lambdaQuery()
                        .eq(Notice::getTargetId, currentUserId)
                        .eq(Notice::getIsRead, DbConsts.READ_NO)
                        .eq(Notice::getIsDeleted, DbConsts.DELETED_NO));
        return unreadNotices;
    }

    /**
     * 列出当前用户的所有通知
     * @return 通知列表
     */
    public List<Notice> listNotices() {
        Long currentUserId = SecurityUtils.getUserId();
        List<Notice> notices = noticeMapper.selectList(
                Wrappers.<Notice>lambdaQuery()
                        .eq(Notice::getTargetId, currentUserId)
                        .eq(Notice::getIsDeleted, DbConsts.DELETED_NO));
        return notices;
    }

    /**
     * 标记通知为已读
     * @param req 包含要标记的通知ID列表的请求
     */
    @Transactional
    public void markNoticeAsRead(NoticeReadMarkReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        int count = noticeMapper.update(
                Wrappers.<Notice>lambdaUpdate()
                        .set(Notice::getIsRead, DbConsts.READ_YES)
                        .eq(Notice::getTargetId, currentUserId)
                        .in(Notice::getId, req.getIds()));
        if (count != req.getIds().size()) throw new BusinessException("通知标记已读失败");
    }

    /**
     * 删除通知
     * @param req 包含要删除的通知ID列表的请求
     */
    @Transactional
    public void deleteNotices(NoticeDeletionReq req) {
        Long currentUserId = SecurityUtils.getUserId();
        int count = noticeMapper.update(
                Wrappers.<Notice>lambdaUpdate()
                        .set(Notice::getIsDeleted, DbConsts.DELETED_YES)
                        .eq(Notice::getTargetId, currentUserId)
                        .in(Notice::getId, req.getIds()));
        if (count != req.getIds().size()) throw new BusinessException("删除通知失败");
    }
}
