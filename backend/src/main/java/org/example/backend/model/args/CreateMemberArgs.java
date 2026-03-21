package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateMemberArgs {
    private Long userId;
    private Long nodeId;
    private Long roleId;
}
