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
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getUserId, userId)
                .eq(Drive::getDriveType, 1)
                .eq(Drive::getNodeId, 0);
        Drive drive = driveMapper.selectOne(driveQuery);
        if (drive == null) throw new BusinessException("Drive does not exist");

        return drive;
    }

    public Long getPersonalDriveId(Long userId) {
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getUserId, userId)
                .eq(Drive::getDriveType, 1);
        Drive drive = driveMapper.selectOne(driveQuery);
        if (drive == null) throw new BusinessException("Drive does not exist");
        return drive.getId();
    }
}
