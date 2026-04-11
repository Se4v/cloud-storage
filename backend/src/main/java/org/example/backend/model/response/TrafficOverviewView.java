package org.example.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrafficOverviewView {
    private Long totalQuota;
    private Long usedQuota;
    private Long totalUpload;
    private Long totalDownload;
}
