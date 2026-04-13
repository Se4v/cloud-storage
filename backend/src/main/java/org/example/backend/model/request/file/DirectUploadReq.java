package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectUploadReq {
    @NotBlank
    private String sha256;
}
