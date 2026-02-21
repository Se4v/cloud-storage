package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectUploadView {
    private String entryName;
    private String sha256;
    private Boolean success;
}
