package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAvatarArgs {
    @NotNull
    private String objectName;
}
