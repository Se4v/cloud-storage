package org.example.backend.model.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class RoleCreationReq {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotNull
    @PositiveOrZero
    private Integer type;
}
