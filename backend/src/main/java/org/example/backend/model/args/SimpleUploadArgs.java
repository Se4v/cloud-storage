package org.example.backend.model.args;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SimpleUploadArgs {
    private Long userId;
    private String sha256;
}
