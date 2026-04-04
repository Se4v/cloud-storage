package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordArgs {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
