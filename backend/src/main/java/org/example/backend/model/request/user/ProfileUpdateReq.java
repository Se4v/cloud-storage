package org.example.backend.model.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateReq {
    @NotBlank
    private String email;
    @NotBlank
    private String mobile;
}
