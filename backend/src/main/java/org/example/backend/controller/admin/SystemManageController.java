package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.UpdateSystemConfigArgs;
import org.example.backend.model.view.SystemConfigView;
import org.example.backend.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemManageController {
    @Autowired
    private SystemService systemService;

    @GetMapping
    public Result<SystemConfigView> getSystemConfigs() {
        Map<String, String> configMap = systemService.getSystemConfigs();

        String loginFailThreshold = configMap.getOrDefault("login_fail_threshold", "0");
        String defaultPassword = configMap.getOrDefault("default_password", "");
        String defaultStorageQuota = configMap.getOrDefault("default_storage_quota", "0");
        String maxFileSize = configMap.getOrDefault("max_file_size", "0");
        String storageWarningThreshold = configMap.getOrDefault("storage_warning_threshold", "0");
        List<String> fileTypeBlacklist = List.of(configMap.getOrDefault("file_type_blacklist", "").split(","));

        SystemConfigView view = SystemConfigView.builder()
                .loginFailThreshold(loginFailThreshold)
                .defaultPassword(defaultPassword)
                .defaultStorageQuota(defaultStorageQuota)
                .maxFileSize(maxFileSize)
                .storageWarningThreshold(storageWarningThreshold)
                .fileTypeBlacklist(fileTypeBlacklist)
                .build();

        return Result.success(view);
    }

    @PostMapping("/update")
    public Result<Void> updateSystemConfigs(@RequestBody UpdateSystemConfigArgs args) {
        systemService.updateSystemConfigs(args);
        return Result.success("");
    }
}
