package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateUserArgs {
    @NotNull
    private Long id;
    @NotBlank
    private String realName;
    @NotBlank
    private String mobile;
    private String email;
    @NotNull
    @PositiveOrZero
    private Long storageQuota;
    @NotNull
    private Boolean isEnabled;
}
