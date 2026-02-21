package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MergeChunksView {
    private String uploadId;
    private Boolean success;
}
