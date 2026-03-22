package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProfileArgs {
    @NotNull
    private Long userId;
    @NotNull
    private String email;
    @NotNull
    private String mobile;
}
