package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Drive;

public interface DriveMapper extends BaseMapper<Drive> {
    Integer increaseUsedQuotaById(Long driveId, Long fileSize);
}
