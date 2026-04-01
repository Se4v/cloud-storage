package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.common.Result;
import org.example.backend.mapper.ConfigMapper;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.model.entity.Config;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.view.DriveDetailView;
import org.example.backend.model.view.DriveOverviewView;
import org.example.backend.model.view.DriveUsageBreakdownView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DriveStatService {
    @Autowired
    private ConfigMapper configMapper;
    @Autowired
    private DriveMapper driveMapper;

    public DriveOverviewView getDriveOverview() {
        LambdaQueryWrapper<Config> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(Config::getConfigKey, "total_quota");
        Config config = configMapper.selectOne(configQuery);
        Long totalQuota = Long.parseLong(config.getConfigValue());

        Map<String, Object> quotaSums = driveMapper.selectQuotaSums();
        Long usedQuota = Long.parseLong(quotaSums.get("sumTotalQuota").toString());
        Long allocatedQuota = usedQuota - Long.parseLong(quotaSums.get("sumUsedQuota").toString());
        Long remainingQuota = totalQuota - usedQuota;

        DriveOverviewView view = DriveOverviewView.builder()
                .totalQuota(totalQuota)
                .allocatedQuota(allocatedQuota)
                .usedQuota(usedQuota)
                .remainingQuota(remainingQuota)
                .build();
        return view;
    }

    public DriveUsageBreakdownView getDriveUsageBreakdown() {
        List<Map<String, Object>> result = driveMapper.selectQuotaSumByType();

        Long enterpriseQuota = 0L;
        Long personalQuota = 0L;
        for (Map<String, Object> map : result) {
            Integer type = (Integer) map.get("type");
            if (type == 1) {
                personalQuota = Long.parseLong(map.get("totalQuota").toString());
            } else if (type == 2) {
                enterpriseQuota = Long.parseLong(map.get("totalQuota").toString());
            }
        }

        DriveUsageBreakdownView view = DriveUsageBreakdownView.builder()
                .enterpriseQuota(enterpriseQuota)
                .personalQuota(personalQuota)
                .build();
        return view;
    }

    public List<DriveDetailView> getEnterpriseDriveDetails() {
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getDriveType, 2);
        List<Drive> drives = driveMapper.selectList(driveQuery);

        List<DriveDetailView> views = drives.stream()
                .map(drive -> {
                    Long remainingQuota = drive.getTotalQuota() - drive.getUsedQuota();
                    return DriveDetailView.builder()
                            .name(drive.getDriveName())
                            .allocatedQuota(drive.getTotalQuota())
                            .usedQuota(drive.getUsedQuota())
                            .remainingQuota(remainingQuota)
                            .build();
                })
                .toList();

        return views;
    }

    public List<DriveDetailView> getPersonalDriveDetails() {
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getDriveType, 1);
        List<Drive> drives = driveMapper.selectList(driveQuery);

        List<DriveDetailView> views = drives.stream()
                .map(drive -> {
                    Long remainingQuota = drive.getTotalQuota() - drive.getUsedQuota();
                    return DriveDetailView.builder()
                            .name(drive.getDriveName())
                            .allocatedQuota(drive.getTotalQuota())
                            .usedQuota(drive.getUsedQuota())
                            .remainingQuota(remainingQuota)
                            .build();
                })
                .toList();

        return views;
    }

}
