package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareView {
    private String shareId;
    private String linkName;
    private String linkKey;
    private String expireTime;
    private String createTime;
}
