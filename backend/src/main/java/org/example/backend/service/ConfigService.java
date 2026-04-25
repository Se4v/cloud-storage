package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.PostConstruct;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.mapper.ConfigMapper;
import org.example.backend.model.entity.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigService {
    private final ConfigMapper configMapper;

    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    public ConfigService(ConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @PostConstruct
    public void init() {
        logger.info("应用启动，初始化配置缓存...");
        loadConfigFromDb();
    }

    private void loadConfigFromDb() {
        try {
            List<Config> configs = configMapper.selectList(
                    Wrappers.<Config>lambdaQuery().eq(Config::getIsEnabled, DbConsts.ENABLED_YES));
            configCache.clear();
            if (configs != null && !configs.isEmpty()) {
                configs.forEach(config -> configCache.put(config.getConfigKey(), config.getConfigValue()));
            }
            logger.info("配置缓存刷新成功，共加载 {} 项配置", configCache.size());
        } catch (Exception e) {
            logger.error("从数据库加载配置失败，使用旧缓存", e);
        }
    }

    public String getDefaultPassword() {
        return configCache.getOrDefault("default_password", "");
    }

    public List<String> getFileTypeBlacklist() {
        return List.of(configCache.getOrDefault("file_type_blacklist", "").split(","));
    }
}
