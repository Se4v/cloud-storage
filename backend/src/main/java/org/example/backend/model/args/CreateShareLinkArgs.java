package org.example.backend.model.args;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateShareLinkArgs {
    private Long entryId;
    private Long driveId;
    private String shareLinkName;
    private String accessCode;
    private Integer shareLinkType;
    private LocalDateTime expireTime;
}
