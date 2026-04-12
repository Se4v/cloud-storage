package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.mapper.ConfigMapper;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.TrafficMapper;
import org.example.backend.model.entity.Config;
import org.example.backend.model.response.stat.FileTypeDistributionResp;
import org.example.backend.model.response.stat.TrafficOverviewResp;
import org.example.backend.model.response.stat.TrendStatisticsResp;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TrafficStatService {
    private final ConfigMapper configMapper;
    private final DriveMapper driveMapper;
    private final TrafficMapper trafficMapper;
    private final EntryMapper entryMapper;

    public TrafficStatService(ConfigMapper configMapper, DriveMapper driveMapper,
            TrafficMapper trafficMapper, EntryMapper entryMapper) {
        this.configMapper = configMapper;
        this.driveMapper = driveMapper;
        this.trafficMapper = trafficMapper;
        this.entryMapper = entryMapper;
    }

    private static final DecimalFormat df = new DecimalFormat("0.0");

    public TrafficOverviewResp getTrafficOverview() {
        Config config = configMapper.selectOne(
                Wrappers.<Config>lambdaQuery()
                        .eq(Config::getConfigKey, "total_quota")
                        .eq(Config::getIsEnabled, 1));
        Long totalQuota = Long.parseLong(config.getConfigValue());

        Map<String, Object> quotaSums = driveMapper.selectQuotaSums();
        Long usedQuota = Long.parseLong(quotaSums.get("sumTotalQuota").toString());

        List<Map<String, Object>> result = trafficMapper.selectTodayTrafficSumByType();
        long totalUpload = 0L;
        long totalDownload = 0L;
        for (Map<String, Object> map : result) {
            Integer type = (Integer) map.get("type");
            if (type == 1) {
                totalUpload = Long.parseLong(map.get("totalTraffic").toString());
            } else if (type == 2) {
                totalDownload = Long.parseLong(map.get("totalTraffic").toString());
            }
        }

        TrafficOverviewResp resp = TrafficOverviewResp.builder()
                .totalQuota(totalQuota)
                .usedQuota(usedQuota)
                .totalUpload(totalUpload)
                .totalDownload(totalDownload)
                .build();

        return resp;
    }

    public List<TrendStatisticsResp> getTrendStatistics() {
        List<Map<String, Object>> result = trafficMapper.selectLast7DaysTraffic();
        if (result == null || result.isEmpty()) return List.of();

        List<TrendStatisticsResp> resp = new ArrayList<>();
        for (Map<String, Object> map : result) {
            Long upload = Long.parseLong(map.getOrDefault("uploadSum", "0").toString());
            Long download = Long.parseLong(map.getOrDefault("downloadSum", "0").toString());
            LocalDate localDate = LocalDate.parse(map.get("statDate").toString());
            LocalDateTime localDateTime = localDate.atStartOfDay();

            TrendStatisticsResp item = TrendStatisticsResp.builder()
                    .date(localDateTime)
                    .upload(upload)
                    .download(download)
                    .fullDate(localDateTime)
                    .build();

            resp.add(item);
        }

        return resp;
    }

    public List<FileTypeDistributionResp> getFileTypeDistribution() {
        List<Map<String, Object>> result = entryMapper.selectFileCategoryStats();
        if (result == null || result.isEmpty()) return List.of();

        Map<String, Object> quotaSums = driveMapper.selectQuotaSums();
        Long usedQuota = Long.parseLong(quotaSums.get("sumUsedQuota").toString());

        List<FileTypeDistributionResp> resp = new ArrayList<>();
        for (Map<String, Object> map : result) {
            Long size = Long.parseLong(map.getOrDefault("totalSize", "0").toString());
            String name = map.get("category").toString();
            String percent = df.format((double) size / usedQuota);

            FileTypeDistributionResp item = FileTypeDistributionResp.builder()
                    .name(name)
                    .size(size)
                    .percent(percent)
                    .build();

            resp.add(item);
        }

        return resp;
    }
}
