package org.example.backend.model.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeReq {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
