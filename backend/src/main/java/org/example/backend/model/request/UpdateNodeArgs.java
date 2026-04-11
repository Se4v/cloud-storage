package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateNodeArgs {
    @NotNull
    private Long id;
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
    @NotNull
    private Integer isEnabled;
    private String adminUsername;
}
