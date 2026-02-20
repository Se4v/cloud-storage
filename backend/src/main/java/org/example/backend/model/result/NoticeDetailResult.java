package org.example.backend.model.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDetailResult {
    private Long id;
    private Long receiverId;
    private String receiverName;
    private Long senderId;
    private String senderName;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
