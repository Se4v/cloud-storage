package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.model.entity.Drive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DriveService {
    @Autowired
    private DriveMapper driveMapper;

    public Drive getPersonalDriveUsage(Long userId) {
        LambdaQueryWrapper<Drive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Drive::getUserId, userId);
        Drive drive = driveMapper.selectOne(queryWrapper);
        if (drive == null) {
            throw new BusinessException("<UNK>");
        }

        return drive;
    }
}
