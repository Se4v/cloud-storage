package org.example.backend.model.args;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateNoticeArgs {
    private String title;
    private String content;
    private Integer type;
    private Long targetId;
}
