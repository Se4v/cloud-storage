package org.example.backend.model.request.sys;

import lombok.Data;

import java.util.List;

@Data
public class SystemConfigUpdateReq {
    private String defaultPassword;
    private String totalQuota;
    private String maxFileSize;
    private String storageWarningThreshold;
    private List<String> fileTypeBlacklist;
}
