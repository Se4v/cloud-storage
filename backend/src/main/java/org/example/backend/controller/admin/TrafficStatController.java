package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.view.TrafficOverviewView;
import org.example.backend.model.view.TrendStatisticsView;
import org.example.backend.service.TrafficStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficStatController {
    @Autowired
    private TrafficStatService trafficStatService;

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
        List<TrendStatisticsView> views = trafficStatService.getTrendStatistics();
        return Result.success(views);
    }
}
