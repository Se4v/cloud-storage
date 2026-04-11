package org.example.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SystemConfigView {
    private String loginFailThreshold;
    private String defaultPassword;
    private String defaultStorageQuota;
    private String maxFileSize;
    private String storageWarningThreshold;
    private List<String> fileTypeBlacklist;
}
