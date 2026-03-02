package org.example.backend.model.result;

import lombok.Data;

@Data
public class NodeDetailResult {
    private Long id;
    private String name;
    private Integer type;
    private Long parentId;
    private String parentName;
}
