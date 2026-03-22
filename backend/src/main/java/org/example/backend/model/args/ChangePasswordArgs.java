package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordArgs {
    @NotNull
    private Long userId;
    @NotNull
    private String oldPassword;
    @NotNull
    private String newPassword;
    @NotNull
    private String confirmPassword;
}
