package org.example.backend.model.response.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MergeChunksView {
    private String sha256;
    private Boolean success;
}
