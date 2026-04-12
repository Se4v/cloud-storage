package org.example.backend.model.response.stat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrendStatisticsResp {
    @JsonFormat(pattern = "MM/dd", timezone = "GMT+8")
    private LocalDateTime date;
    private Long upload;
    private Long download;
    @JsonFormat(pattern = "MM月dd日", timezone = "GMT+8")
    private LocalDateTime fullDate;
}
