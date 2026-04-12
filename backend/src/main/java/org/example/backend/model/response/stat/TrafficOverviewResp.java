package org.example.backend.model.response.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrafficOverviewResp {
    private Long totalQuota;
    private Long usedQuota;
    private Long totalUpload;
    private Long totalDownload;
}
