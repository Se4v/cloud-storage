package org.example.backend.model.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecycleDetailResult {
    private Long entryId;
    private Long driveId;
    private String driveName;
    private Integer driveType;
    private String entryName;
    private Integer entryType;
    private Long fileSize;
    private LocalDateTime deletedAt;
    private LocalDateTime expiredAt;
}
