package org.example.backend.model.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareDetailResult {
    private Long id;
    private String linkName;
    private Integer entryType;
    private String linkKey;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private Boolean isProtected;
}
