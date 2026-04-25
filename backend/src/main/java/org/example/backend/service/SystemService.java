package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.ConfigMapper;
import org.example.backend.model.request.sys.SystemConfigUpdateReq;
import org.example.backend.model.entity.Config;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemService {
    private final ConfigMapper configMapper;

    public SystemService(ConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /**
     * 获取系统配置
     * @return 系统配置映射
     */
    public Map<String, String> getSystemConfigs() {
        List<Config> configs = configMapper.selectList(
                Wrappers.<Config>lambdaQuery()
                        .eq(Config::getIsEnabled, DbConsts.ENABLED_YES));
        Map<String, String> configMap = configs.stream()
                .collect(Collectors.toMap(Config::getConfigKey, Config::getConfigValue, (a, b) -> a));
        return configMap;
    }

    /**
     * 更新系统配置
     * @param req 系统配置更新请求
     */
    @Transactional
    public void updateSystemConfigs(SystemConfigUpdateReq req) {
        // 查询配置项
        List<Config> configs = configMapper.selectList(
                Wrappers.<Config>lambdaQuery().eq(Config::getIsEnabled, DbConsts.ENABLED_YES));
        Map<String, Long> configMap = configs.stream()
                .collect(Collectors.toMap(Config::getConfigKey, Config::getId));

        List<Config> updateConfigList = new ArrayList<>();

        updateConfigList.add(Config.builder()
                .id(configMap.get("default_password"))
                .configKey("default_password")
                .configValue(req.getDefaultPassword())
                .build());

        updateConfigList.add(Config.builder()
                .id(configMap.get("total_quota"))
                .configKey("total_quota")
                .configValue(req.getTotalQuota())
                .build());

        updateConfigList.add(Config.builder()
                .id(configMap.get("max_file_size"))
                .configKey("max_file_size")
                .configValue(req.getMaxFileSize())
                .build());

        updateConfigList.add(Config.builder()
                .id(configMap.get("storage_warning_threshold"))
                .configKey("storage_warning_threshold")
                .configValue(req.getStorageWarningThreshold())
                .build());

        String blackList = req.getFileTypeBlacklist().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
        updateConfigList.add(Config.builder()
                .id(configMap.get("file_type_blacklist"))
                .configKey("file_type_blacklist")
                .configValue(blackList)
                .build());

        List<BatchResult> results = configMapper.updateById(updateConfigList);
        int count = results.stream()
                .flatMapToInt(result -> Arrays.stream(result.getUpdateCounts()))
                .sum();
        if (count != updateConfigList.size()) throw new BusinessException("");
    }
}
