package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateUserArgs {
    @NotBlank
    private String username;
    @NotBlank
    private String realName;
    @NotBlank
    private String mobile;
    @NotNull
    @PositiveOrZero
    private Long storageQuota;
}
