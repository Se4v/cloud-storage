package org.example.backend.model.request.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class PasswordResetReq {
    @NotNull
    @PositiveOrZero
    private Long userId;
}
