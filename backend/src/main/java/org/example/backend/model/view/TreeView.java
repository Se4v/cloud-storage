package org.example.backend.model.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class TreeView {
    private String id;
    private String label;
    private String type;
    private List<TreeView> children;

    // 辅助字段
    @JsonIgnore
    private Long parentId;                // 父节点ID，用于构建树
    @JsonIgnore
    private Integer depth;                // 层级深度，可选
}
