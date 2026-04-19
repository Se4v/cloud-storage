package org.example.backend.model.request.share;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkCheckReq {
    @NotBlank
    private String linkKey;
    @NotBlank
    private String accessCode;
}
