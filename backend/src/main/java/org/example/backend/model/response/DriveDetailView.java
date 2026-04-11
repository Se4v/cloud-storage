package org.example.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveDetailView {
    private String name;
    private Long allocatedQuota;
    private Long usedQuota;
    private Long remainingQuota;
}
