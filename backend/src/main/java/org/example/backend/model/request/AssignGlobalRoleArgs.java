package org.example.backend.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignGlobalRoleArgs {
    @NotNull
    private Long userId;
    @NotEmpty
    private List<Long> roleIds;
}
