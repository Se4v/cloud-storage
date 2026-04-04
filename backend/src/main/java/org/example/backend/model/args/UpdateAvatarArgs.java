package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAvatarArgs {
    @NotBlank
    private String objectName;
}
