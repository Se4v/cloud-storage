package org.example.backend.model.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareDetailResult {
    private Long shareId;
    private String linkName;
    private String linkKey;
    private Boolean isProtected;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
