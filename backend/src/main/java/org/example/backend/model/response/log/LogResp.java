package org.example.backend.model.response.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LogResp {
    private Long id;
    private String username;
    private String realName;
    private String operationType;
    private String detail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operationTime;
    private Integer success;
}
