package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
public class ShareView {
    private String id;
    private String name;
    private String type;
    private String key;
    private String expireTime;
    private String createTime;
    private Boolean isProtected;
}
