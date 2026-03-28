package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SimpleUploadArgs {
    @NotNull
    private Long userId;
    @NotBlank
    private String sha256;
}
