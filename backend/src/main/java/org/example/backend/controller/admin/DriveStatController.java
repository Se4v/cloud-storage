package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.view.DriveDetailView;
import org.example.backend.model.view.DriveOverviewView;
import org.example.backend.model.view.DriveUsageBreakdownView;
import org.example.backend.service.DriveStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drive-stat")
public class DriveStatController {
    @Autowired
    private DriveStatService driveStatService;

    @GetMapping("/overview")
    public Result<?> getDriveOverview() {
        DriveOverviewView view = driveStatService.getDriveOverview();
        return Result.success(view);
    }

    @GetMapping("/breakdown")
    public Result<?> getDriveUsageBreakdown() {
        DriveUsageBreakdownView view = driveStatService.getDriveUsageBreakdown();
        return Result.success(view);
    }

    @GetMapping("/enterprise")
    public Result<?> getEnterpriseDriveDetails() {
        List<DriveDetailView> views = driveStatService.getEnterpriseDriveDetails();
        return Result.success(views);
    }

    @GetMapping("/personal")
    public Result<?> getPersonalDriveDetails() {
        List<DriveDetailView> views = driveStatService.getPersonalDriveDetails();
        return Result.success(views);
    }
}
