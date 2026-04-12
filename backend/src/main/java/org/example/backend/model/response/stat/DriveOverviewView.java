package org.example.backend.model.response.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveOverviewView {
    private Long totalQuota;
    private Long allocatedQuota;
    private Long usedQuota;
    private Long remainingQuota;
}
