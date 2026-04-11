package org.example.backend.model.request.sys;

import lombok.Data;

import java.util.List;

@Data
public class SystemConfigUpdateReq {
    private String loginFailThreshold;
    private String defaultPassword;
    private String defaultStorageQuota;
    private String maxFileSize;
    private String storageWarningThreshold;
    private List<String> fileTypeBlacklist;
}
