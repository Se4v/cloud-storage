package org.example.backend.model.args;

import lombok.Data;

@Data
public class UpdateAnnouncementArgs {
    private String id;
    private String title;
    private String content;
    private String expiredTime;
}
