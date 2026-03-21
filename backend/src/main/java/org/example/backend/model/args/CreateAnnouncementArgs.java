package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateAnnouncementArgs {
    private String title;
    private String content;
    private String expireTime;
}
