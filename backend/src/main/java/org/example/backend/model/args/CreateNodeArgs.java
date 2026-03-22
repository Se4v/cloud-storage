package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNodeArgs {
    @NotNull
    private String name;
    @NotNull
    private Integer type;
    @NotNull
    private Long parentId;
    @NotNull
    private Long storageQuota;
    @NotNull
    private String adminUsername;
}
