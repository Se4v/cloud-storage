package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateNodeArgs {
    @NotBlank
    private String name;
    @NotNull
    private Integer type;
    @NotNull
    @PositiveOrZero
    private Long parentId;
    @NotNull
    @PositiveOrZero
    private Long storageQuota;
    @NotBlank
    private String adminUsername;
}
