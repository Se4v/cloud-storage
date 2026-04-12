package org.example.backend.model.response.share;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShareLinkResp {
    @NotNull
    private Long id;
    @NotNull
    private Integer fileType;
    @NotNull
    private String linkName;
    @NotNull
    private String linkKey;
    @NotNull
    private Integer linkType;
    @NotNull
    private String accessCode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
