package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateRoleArgs {
    @NotNull
    @PositiveOrZero
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotNull
    @PositiveOrZero
    private Integer isEnabled;
}
