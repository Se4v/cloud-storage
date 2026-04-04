package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileArgs {
    @NotBlank
    private String email;
    @NotBlank
    private String mobile;
}
