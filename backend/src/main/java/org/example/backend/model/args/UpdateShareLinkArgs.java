package org.example.backend.model.args;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateShareLinkArgs {
    private Long shareId;
    private String shareLinkName;
    private String accessCode;
    private Integer shareLinkType;
    private Integer valid;
    private LocalDateTime expireTime;
}
