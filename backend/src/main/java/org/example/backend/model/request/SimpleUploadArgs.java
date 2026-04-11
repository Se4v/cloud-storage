package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SimpleUploadArgs {
    @NotBlank
    private String sha256;
}
