package org.example.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveUsageBreakdownView {
    private Long enterpriseQuota;
    private Long personalQuota;
}
