package org.example.backend.model.args;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateShareLinkArgs {
    private Long shareId;
    private String linkName;
    private Integer linkType;
    private String accessCode;
    private LocalDateTime expiredAt;
}
