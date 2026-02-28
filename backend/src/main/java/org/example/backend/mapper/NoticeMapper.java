package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Notice;

import java.util.List;

public interface NoticeMapper extends BaseMapper<Notice> {

    Integer updateReadStatus(List<Long> noticeIds, Integer readStatus);
}
