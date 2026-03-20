package org.example.backend.model.args;

import lombok.Data;

@Data
public class UpdateRoleArgs {
    private Long id;
    private String name;
    private String code;
    private Boolean isEnabled;
}
