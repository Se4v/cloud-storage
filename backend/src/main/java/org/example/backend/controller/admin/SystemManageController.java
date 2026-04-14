package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.sys.SystemConfigUpdateReq;
import org.example.backend.model.response.config.SystemConfigResp;
import org.example.backend.service.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemManageController {
    private final SystemService systemService;

    public SystemManageController(SystemService systemService) {
        this.systemService = systemService;
    }

    /**
     * 获取系统全局配置
     * @return 系统配置详情响应结果
     */
    @GetMapping
    public Result<SystemConfigResp> getSystemConfigs() {
        Map<String, String> configMap = systemService.getSystemConfigs();

        String loginFailThreshold = configMap.getOrDefault("login_fail_threshold", "0");
        String defaultPassword = configMap.getOrDefault("default_password", "");
        String defaultStorageQuota = configMap.getOrDefault("default_storage_quota", "0");
        String maxFileSize = configMap.getOrDefault("max_file_size", "0");
        String storageWarningThreshold = configMap.getOrDefault("storage_warning_threshold", "0");
        List<String> fileTypeBlacklist = List.of(configMap.getOrDefault("file_type_blacklist", "").split(","));

        SystemConfigResp resp = SystemConfigResp.builder()
                .loginFailThreshold(loginFailThreshold)
                .defaultPassword(defaultPassword)
                .defaultStorageQuota(defaultStorageQuota)
                .maxFileSize(maxFileSize)
                .storageWarningThreshold(storageWarningThreshold)
                .fileTypeBlacklist(fileTypeBlacklist)
                .build();

        return Result.success(resp);
    }

    /**
     * 更新系统全局配置
     * @param req 系统配置更新请求参数
     * @return 统一响应结果
     */
    @PostMapping("/update")
    public Result<Void> updateSystemConfigs(@RequestBody SystemConfigUpdateReq req) {
        systemService.updateSystemConfigs(req);
        return Result.success();
    }
}
