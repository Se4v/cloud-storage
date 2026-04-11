package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateMemberArgs {
    @NotBlank
    private String username;
    @NotNull
    @PositiveOrZero
    private Long nodeId;
    @NotNull
    @PositiveOrZero
    private Long roleId;
}
