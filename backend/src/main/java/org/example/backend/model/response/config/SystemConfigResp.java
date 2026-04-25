package org.example.backend.model.response.config;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SystemConfigResp {
    private String defaultPassword;
    private String totalQuota;
    private String maxFileSize;
    private String storageWarningThreshold;
    private List<String> fileTypeBlacklist;
}
