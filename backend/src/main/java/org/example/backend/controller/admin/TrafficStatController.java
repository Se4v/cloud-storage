package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.response.FileTypeDistributionView;
import org.example.backend.model.response.TrafficOverviewView;
import org.example.backend.model.response.TrendStatisticsView;
import org.example.backend.service.TrafficStatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficStatController {
    private final TrafficStatService trafficStatService;

    public TrafficStatController(TrafficStatService trafficStatService) {
        this.trafficStatService = trafficStatService;
    }

    @GetMapping("/overview")
    public Result<?> getTrafficOverview() {
        TrafficOverviewView view = trafficStatService.getTrafficOverview();
        return Result.success(view);
    }

    @GetMapping("/trend")
    public Result<?> getTrendStatistics() {
        List<TrendStatisticsView> views = trafficStatService.getTrendStatistics();
        return Result.success(views);
    }

    @GetMapping("/distribution")
    public Result<?> getFileTypeDistribution() {
        List<FileTypeDistributionView> views = trafficStatService.getFileTypeDistribution();
        return Result.success(views);
    }
}
