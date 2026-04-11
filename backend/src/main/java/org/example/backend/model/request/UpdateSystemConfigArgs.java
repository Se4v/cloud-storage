package org.example.backend.model.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateSystemConfigArgs {
    private String loginFailThreshold;
    private String defaultPassword;
    private String defaultStorageQuota;
    private String maxFileSize;
    private String storageWarningThreshold;
    private List<String> fileTypeBlacklist;
}
