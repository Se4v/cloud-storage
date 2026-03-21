package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.NoticeMapper;
import org.example.backend.model.args.CreateAnnouncementArgs;
import org.example.backend.model.args.DeleteAnnouncementArgs;
import org.example.backend.model.args.UpdateAnnouncementArgs;
import org.example.backend.model.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {
    @Autowired
    private NoticeMapper noticeMapper;

    @Transactional
    public void createAnnouncement(CreateAnnouncementArgs args) {
        if (args == null) throw new BusinessException("");
        if (args.getTitle() == null || args.getContent() == null) throw new BusinessException("");

        Notice notice = Notice.builder()
                .title(args.getTitle())
                .content(args.getContent())
                .type(1)
                .expiredAt(LocalDateTime.now().plusDays(15))
                .build();

        int count = noticeMapper.insert(notice);
        if (count != 1) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void deleteAnnouncement(DeleteAnnouncementArgs args) {
        if (args == null) throw new BusinessException("");
        if (args.getAnnouncementIds() == null) throw new BusinessException("");

        LambdaUpdateWrapper<Notice> noticeUpdate = new LambdaUpdateWrapper<>();
        noticeUpdate.in(Notice::getId, args.getAnnouncementIds())
                .set(Notice::getDeleted, 1);
        int count = noticeMapper.update(noticeUpdate);
        if (count != args.getAnnouncementIds().size()) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void updateAnnouncement(UpdateAnnouncementArgs args) {
        if (args == null) throw new BusinessException("");

        LambdaUpdateWrapper<Notice> noticeUpdate = new LambdaUpdateWrapper<>();
        noticeUpdate.set(args.getTitle() != null, Notice::getTitle, args.getTitle())
                .set(args.getContent() != null, Notice::getContent, args.getContent())
                .set(args.getExpiredTime() != null, Notice::getExpiredAt, LocalDateTime.parse(args.getExpiredTime()))
                .eq(Notice::getId, args.getId());

        int count = noticeMapper.update(noticeUpdate);
        if (count != 1) throw new BusinessException("<UNK>");
    }

    public List<Notice> listAllAnnouncement() {
        LambdaQueryWrapper<Notice> noticeQuery = new LambdaQueryWrapper<>();
        noticeQuery.eq(Notice::getTargetId, 0)
                .eq(Notice::getType, 1)
                .eq(Notice::getDeleted, 0);

        return noticeMapper.selectList(noticeQuery);
    }

}
