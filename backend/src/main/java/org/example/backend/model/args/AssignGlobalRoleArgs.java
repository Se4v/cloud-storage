package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class AssignGlobalRoleArgs {
    private Long userId;
    private List<Long> roleIds;
}
