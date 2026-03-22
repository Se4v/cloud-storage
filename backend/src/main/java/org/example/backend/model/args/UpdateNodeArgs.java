package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateNodeArgs {
    @NotNull
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Integer type;
    @NotNull
    private Long parentId;
    @NotNull
    @PositiveOrZero
    private Long storageQuota;
    @NotNull
    private Integer isEnabled;
    @NotNull
    private String adminUsername;
}
