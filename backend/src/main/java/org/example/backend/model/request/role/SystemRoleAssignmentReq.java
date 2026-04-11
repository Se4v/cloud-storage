package org.example.backend.model.request.role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SystemRoleAssignmentReq {
    @NotNull
    private Long userId;
    @NotEmpty
    private List<Long> roleIds;
}
