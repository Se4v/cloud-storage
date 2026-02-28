package org.example.backend.model.args;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateShareLinkArgs {
    private Long shareId;
    private String linkName;
    private String accessCode;
    private Integer linkType;
    private Integer deleted;
    private LocalDateTime expireTime;
}
