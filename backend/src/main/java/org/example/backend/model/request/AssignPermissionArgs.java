package org.example.backend.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionArgs {
    @NotNull
    @PositiveOrZero
    private Long roleId;
    @NotEmpty
    private List<Long> permissionIds;
}
