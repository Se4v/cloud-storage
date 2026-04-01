package org.example.backend.model.view;

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
