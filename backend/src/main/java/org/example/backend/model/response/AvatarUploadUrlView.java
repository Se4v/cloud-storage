package org.example.backend.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvatarUploadUrlView {
    @NotNull
    private String uploadUrl;
    @NotNull
    private String objectName;
}
