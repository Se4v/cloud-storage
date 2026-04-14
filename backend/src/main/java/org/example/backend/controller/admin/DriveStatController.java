package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.response.drive.DriveDetailResp;
import org.example.backend.model.response.stat.DriveOverviewResp;
import org.example.backend.model.response.stat.DriveUsageBreakdownResp;
import org.example.backend.service.DriveStatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drive-stat")
public class DriveStatController {
    private final DriveStatService driveStatService;

    public DriveStatController(DriveStatService driveStatService) {
        this.driveStatService = driveStatService;
    }

    /**
     * 获取空间总览数据
     * @return 空间概览统计结果
     */
    @GetMapping("/overview")
    public Result<?> getDriveOverview() {
        DriveOverviewResp resp = driveStatService.getDriveOverview();
        return Result.success(resp);
    }

    /**
     * 获取空间使用分类明细
     * @return 空间使用分类统计结果
     */
    @GetMapping("/breakdown")
    public Result<?> getDriveUsageBreakdown() {
        DriveUsageBreakdownResp resp = driveStatService.getDriveUsageBreakdown();
        return Result.success(resp);
    }

    /**
     * 获取企业空间详情列表
     * @return 企业空间详情集合
     */

    @GetMapping("/enterprise")
    public Result<?> getEnterpriseDriveDetails() {
        List<DriveDetailResp> resp = driveStatService.getEnterpriseDriveDetails();
        return Result.success(resp);
    }

    /**
     * 获取个人空间详情列表
     * @return 个人空间详情集合
     */
    @GetMapping("/personal")
    public Result<?> getPersonalDriveDetails() {
        List<DriveDetailResp> resp = driveStatService.getPersonalDriveDetails();
        return Result.success(resp);
    }
}
