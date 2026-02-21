package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Share;
import org.example.backend.model.result.ShareDetailResult;

import java.util.List;

public interface ShareMapper extends BaseMapper<Share> {
    List<ShareDetailResult> selectAllByUserId(Long userId);

    Integer updateByShareId(Share share, Long shareId);
}
