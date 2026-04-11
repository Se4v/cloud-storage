package org.example.backend.model.request;

import lombok.Data;

@Data
public class CreateMemberArgs {
    private String username;
    private Long nodeId;
    private Long roleId;
}
