package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoticeView {
    private String noticeId;
    private String title;
    private String content;
    private String sendTime;
}
