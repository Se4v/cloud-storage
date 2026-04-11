package org.example.backend.model.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarUpdateReq {
    @NotBlank
    private String objectName;
}
