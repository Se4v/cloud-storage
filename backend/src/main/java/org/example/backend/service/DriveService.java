package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.model.entity.Drive;
import org.springframework.stereotype.Service;

@Service
public class DriveService {
    private final DriveMapper driveMapper;

    public DriveService(DriveMapper driveMapper) {
        this.driveMapper = driveMapper;
    }

    public Drive getPersonalDriveUsage() {
        Long currentUserId = SecurityUtils.getUserId();
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getUserId, currentUserId)
                        .eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_PERSONAL)
                        .eq(Drive::getNodeId, 0));
        if (drive == null) throw new BusinessException("Drive does not exist");
        return drive;
    }

    public Long getPersonalDriveId() {
        Long currentUserId = SecurityUtils.getUserId();
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getUserId, currentUserId)
                        .eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_PERSONAL));
        if (drive == null) throw new BusinessException("Drive does not exist");
        return drive.getId();
    }
}
