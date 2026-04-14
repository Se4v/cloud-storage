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

    /**
     * 获取当前登录用户的个人空间使用信息
     * @return 个人空间详情
     */
    public Drive getPersonalDriveUsage() {
        Long currentUserId = SecurityUtils.getUserId();
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getUserId, currentUserId)
                        .eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_PERSONAL)
                        .eq(Drive::getNodeId, 0));
        if (drive == null) throw new BusinessException("该存储空间不存在");
        return drive;
    }

    /**
     * 获取当前登录用户的个人空间ID
     * @return 个人空间ID
     */
    public Long getPersonalDriveId() {
        Long currentUserId = SecurityUtils.getUserId();
        Drive drive = driveMapper.selectOne(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getUserId, currentUserId)
                        .eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_PERSONAL));
        if (drive == null) throw new BusinessException("该存储空间不存在");
        return drive.getId();
    }
}
