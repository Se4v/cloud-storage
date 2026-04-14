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

    /**
     * 获取流量总览数据
     * @return 流量概览统计结果
     */
    @GetMapping("/overview")
    public Result<?> getTrafficOverview() {
        TrafficOverviewResp resp = trafficStatService.getTrafficOverview();
        return Result.success(resp);
    }

    /**
     * 获取流量趋势统计数据
     * @return 流量趋势统计列表
     */
    @GetMapping("/trend")
    public Result<?> getTrendStatistics() {
        List<TrendStatisticsResp> resp = trafficStatService.getTrendStatistics();
        return Result.success(resp);
    }

    /**
     * 获取文件类型分布数据
     * @return 文件类型分布统计列表
     */
    @GetMapping("/distribution")
    public Result<?> getFileTypeDistribution() {
        List<FileTypeDistributionResp> resp = trafficStatService.getFileTypeDistribution();
        return Result.success(resp);
    }
}
