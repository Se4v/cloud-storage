package org.example.backend.model.response.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriveUsageBreakdownResp {
    private Long enterpriseQuota;
    private Long personalQuota;
}
