package org.example.backend.model.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecycleDetailResult {
    private Long entryId;
    private Long driveId;
    private String entryName;
    private Integer entryType;
    private Long fileSize;
    private Integer status;
    private LocalDateTime deletedAt;
    private LocalDateTime expiredAt;

    private String driveName;
    private Integer driveType;
}
