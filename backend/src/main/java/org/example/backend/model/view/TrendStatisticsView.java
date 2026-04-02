package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrendStatisticsView {
    private LocalDateTime date;
    private Long upload;
    private Long download;
    private LocalDateTime fullDate;
}
