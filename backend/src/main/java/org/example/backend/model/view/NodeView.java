package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeView {
    private Long id;
    private String name;
    private String type;
    private Long parentId;
    private String parentName;
    private Long storageQuota;
    private String adminUsername;
}
