package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginArgs {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
