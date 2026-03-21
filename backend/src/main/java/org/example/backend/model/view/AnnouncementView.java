package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnouncementView {
    private Long id;
    private String title;
    private String content;
    private String expiredTime;
}
