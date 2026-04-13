package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChunkMergeReq {
    @NotBlank
    private String sha256;
}
