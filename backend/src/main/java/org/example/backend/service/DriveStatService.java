package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.mapper.ConfigMapper;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.model.entity.Config;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.response.drive.DriveDetailResp;
import org.example.backend.model.response.stat.DriveOverviewResp;
import org.example.backend.model.response.stat.DriveUsageBreakdownResp;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DriveStatService {
    private final ConfigMapper configMapper;
    private final DriveMapper driveMapper;

    public DriveStatService(ConfigMapper configMapper, DriveMapper driveMapper) {
        this.configMapper = configMapper;
        this.driveMapper = driveMapper;
    }

    public DriveOverviewResp getDriveOverview() {
        Config config = configMapper.selectOne(
                Wrappers.<Config>lambdaQuery()
                        .eq(Config::getConfigKey, "total_quota")
                        .eq(Config::getIsEnabled, DbConsts.ENABLED_YES));
        Long totalQuota = Long.parseLong(config.getConfigValue());

        Map<String, Object> quotaSums = driveMapper.selectQuotaSums();
        Long usedQuota = Long.parseLong(quotaSums.get("sumTotalQuota").toString());
        Long allocatedQuota = usedQuota - Long.parseLong(quotaSums.get("sumUsedQuota").toString());
        Long remainingQuota = totalQuota - usedQuota;

        DriveOverviewResp resp = DriveOverviewResp.builder()
                .totalQuota(totalQuota)
                .allocatedQuota(allocatedQuota)
                .usedQuota(usedQuota)
                .remainingQuota(remainingQuota)
                .build();
        return resp;
    }

    public DriveUsageBreakdownResp getDriveUsageBreakdown() {
        List<Map<String, Object>> result = driveMapper.selectQuotaSumByType();

        Long enterpriseQuota = 0L;
        Long personalQuota = 0L;
        for (Map<String, Object> map : result) {
            Integer type = (Integer) map.get("type");
            if (type == DbConsts.DRIVE_TYPE_PERSONAL) {
                personalQuota = Long.parseLong(map.get("totalQuota").toString());
            } else if (type == DbConsts.DRIVE_TYPE_ENTERPRISE) {
                enterpriseQuota = Long.parseLong(map.get("totalQuota").toString());
            }
        }

        DriveUsageBreakdownResp resp = DriveUsageBreakdownResp.builder()
                .enterpriseQuota(enterpriseQuota)
                .personalQuota(personalQuota)
                .build();
        return resp;
    }

    public List<DriveDetailResp> getEnterpriseDriveDetails() {
        List<Drive> drives = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery().eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_ENTERPRISE));

        List<DriveDetailResp> resp = drives.stream()
                .map(drive -> {
                    Long remainingQuota = drive.getTotalQuota() - drive.getUsedQuota();
                    return DriveDetailResp.builder()
                            .name(drive.getDriveName())
                            .allocatedQuota(drive.getTotalQuota())
                            .usedQuota(drive.getUsedQuota())
                            .remainingQuota(remainingQuota)
                            .build();
                })
                .toList();

        return resp;
    }

    public List<DriveDetailResp> getPersonalDriveDetails() {
        List<Drive> drives = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery().eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_PERSONAL));

        List<DriveDetailResp> resp = drives.stream()
                .map(drive -> {
                    Long remainingQuota = drive.getTotalQuota() - drive.getUsedQuota();
                    return DriveDetailResp.builder()
                            .name(drive.getDriveName())
                            .allocatedQuota(drive.getTotalQuota())
                            .usedQuota(drive.getUsedQuota())
                            .remainingQuota(remainingQuota)
                            .build();
                })
                .toList();

        return resp;
    }

}
