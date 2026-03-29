package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateLinkArgs {
    @NotNull
    private Long id;
    @NotNull
    private Long driveId;
    @NotBlank
    private String linkName;
    @NotNull
    private Integer linkType;
    private String accessCode;
    @NotNull
    private LocalDateTime expireTime;
}
