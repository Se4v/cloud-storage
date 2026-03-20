package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionArgs {
    private Long roleId;
    private List<Long> permissionIds;
}
