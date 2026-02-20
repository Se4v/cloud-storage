package org.example.backend.model.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareDetailResult {
    private Long shareId;
    private Long driveId;
    private String driveName;
    private String shareLinkName;
    private String shareLinkKey;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
