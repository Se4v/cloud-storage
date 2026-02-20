package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.result.NoticeDetailResult;

import java.util.List;

public interface NoticeMapper extends BaseMapper<Notice> {
    List<NoticeDetailResult> selectUnreadNotices(Long userId);

    List<NoticeDetailResult> selectAllNotices(Long userId);

    Integer updateReadStatus(List<Long> noticeIds, Integer readStatus);

    Integer deleteNotices(List<Long> noticeIds);
}
