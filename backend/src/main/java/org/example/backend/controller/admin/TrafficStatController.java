package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.response.stat.FileTypeDistributionResp;
import org.example.backend.model.response.stat.TrafficOverviewResp;
import org.example.backend.model.response.stat.TrendStatisticsResp;
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
        TrafficOverviewResp resp = trafficStatService.getTrafficOverview();
        return Result.success(resp);
    }

    @GetMapping("/trend")
    public Result<?> getTrendStatistics() {
        List<TrendStatisticsResp> resp = trafficStatService.getTrendStatistics();
        return Result.success(resp);
    }

    @GetMapping("/distribution")
    public Result<?> getFileTypeDistribution() {
        List<FileTypeDistributionResp> resp = trafficStatService.getFileTypeDistribution();
        return Result.success(resp);
    }
}
