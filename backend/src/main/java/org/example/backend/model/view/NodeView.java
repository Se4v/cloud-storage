package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeView {
    private String id;
    private String name;
    private String type;
    private String parentId;
    private String parentName;
}
