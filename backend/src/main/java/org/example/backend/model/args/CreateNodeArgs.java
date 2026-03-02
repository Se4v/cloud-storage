package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateNodeArgs {
    private String name;
    private Integer type;
    private Long parentId;
}
