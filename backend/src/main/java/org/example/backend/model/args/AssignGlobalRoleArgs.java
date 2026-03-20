package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class AssignGlobalRoleArgs {
    private Long id;
    private List<Long> roleIds;
}
