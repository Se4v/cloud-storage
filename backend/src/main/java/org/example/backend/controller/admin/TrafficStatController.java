package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.service.TrafficStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traffic")
public class TrafficStatController {
    @Autowired
    private TrafficStatService trafficStatService;

    @GetMapping("/overview")
    public Result<?> getTrafficOverview() {
        return null;
    }

    @GetMapping("/trend")
    public Result<?> getTrendStatistics() {
        return null;
    }

    @GetMapping("/distribution")
    public Result<?> getFileTypeDistribution() {
        return null;
    }
}
