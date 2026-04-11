package org.example.backend.model.request.share;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LinkUpdateReq {
    @NotNull
    private Long id;
    @NotBlank
    private String linkName;
    @NotNull
    private Integer linkType;
    private String accessCode;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;
}
