package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.response.DriveDetailView;
import org.example.backend.model.response.DriveOverviewView;
import org.example.backend.model.response.DriveUsageBreakdownView;
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

    @GetMapping("/overview")
    public Result<?> getDriveOverview() {
        DriveOverviewView resp = driveStatService.getDriveOverview();
        return Result.success(resp);
    }

    @GetMapping("/breakdown")
    public Result<?> getDriveUsageBreakdown() {
        DriveUsageBreakdownView resp = driveStatService.getDriveUsageBreakdown();
        return Result.success(resp);
    }

    @GetMapping("/enterprise")
    public Result<?> getEnterpriseDriveDetails() {
        List<DriveDetailView> resp = driveStatService.getEnterpriseDriveDetails();
        return Result.success(resp);
    }

    @GetMapping("/personal")
    public Result<?> getPersonalDriveDetails() {
        List<DriveDetailView> resp = driveStatService.getPersonalDriveDetails();
        return Result.success(resp);
    }
}
