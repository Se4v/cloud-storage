package org.example.backend.model.args;

import lombok.Data;

@Data
public class UpdateNoticeArgs {
    private String id;
    private String title;
    private String content;
    private String expiredAt;
}
