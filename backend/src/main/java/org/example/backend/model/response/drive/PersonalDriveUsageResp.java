package org.example.backend.model.response.drive;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonalDriveUsageResp {
    private Long usedQuota;
    private Long totalQuota;
}
