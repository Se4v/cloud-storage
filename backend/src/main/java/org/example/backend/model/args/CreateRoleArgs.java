package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateRoleArgs {
    private String name;
    private String code;
    private Integer type;
}
