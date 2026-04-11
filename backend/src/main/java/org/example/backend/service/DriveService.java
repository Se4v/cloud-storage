package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.model.entity.Drive;
import org.springframework.stereotype.Service;

@Service
public class DriveService {
    private final DriveMapper driveMapper;

    public DriveService(DriveMapper driveMapper) {
        this.driveMapper = driveMapper;
    }

    public Drive getPersonalDriveUsage(Long userId) {
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getUserId, userId)
                        .eq(Drive::getDriveType, 1)
                        .eq(Drive::getNodeId, 0));
        if (drive == null) throw new BusinessException("Drive does not exist");
        return drive;
    }

    public Long getPersonalDriveId(Long userId) {
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getUserId, userId)
                        .eq(Drive::getDriveType, 1));
        if (drive == null) throw new BusinessException("Drive does not exist");
        return drive.getId();
    }
}
