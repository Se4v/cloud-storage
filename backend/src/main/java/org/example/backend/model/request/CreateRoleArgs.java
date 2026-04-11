package org.example.backend.model.request;

import lombok.Data;

@Data
public class CreateRoleArgs {
    private String name;
    private String code;
    private String type;
}
