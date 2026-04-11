package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAvatarArgs {
    @NotBlank
    private String objectName;
}
