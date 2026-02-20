package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareView {
    private String shareId;
    private String driveId;
    private String driveName;
    private String shareLinkName;
    private String shareLinkKey;
    private String expireTime;
    private String createTime;
}
