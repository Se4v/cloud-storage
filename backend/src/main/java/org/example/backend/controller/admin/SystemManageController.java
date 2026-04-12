package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.sys.SystemConfigUpdateReq;
import org.example.backend.model.response.config.SystemConfigView;
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

    @GetMapping
    public Result<SystemConfigView> getSystemConfigs() {
        Map<String, String> configMap = systemService.getSystemConfigs();

        String loginFailThreshold = configMap.getOrDefault("login_fail_threshold", "0");
        String defaultPassword = configMap.getOrDefault("default_password", "");
        String defaultStorageQuota = configMap.getOrDefault("default_storage_quota", "0");
        String maxFileSize = configMap.getOrDefault("max_file_size", "0");
        String storageWarningThreshold = configMap.getOrDefault("storage_warning_threshold", "0");
        List<String> fileTypeBlacklist = List.of(configMap.getOrDefault("file_type_blacklist", "").split(","));

        SystemConfigView resp = SystemConfigView.builder()
                .loginFailThreshold(loginFailThreshold)
                .defaultPassword(defaultPassword)
                .defaultStorageQuota(defaultStorageQuota)
                .maxFileSize(maxFileSize)
                .storageWarningThreshold(storageWarningThreshold)
                .fileTypeBlacklist(fileTypeBlacklist)
                .build();

        return Result.success(resp);
    }

    @PostMapping("/update")
    public Result<Void> updateSystemConfigs(@RequestBody SystemConfigUpdateReq req) {
        systemService.updateSystemConfigs(req);
        return Result.success();
    }
}
