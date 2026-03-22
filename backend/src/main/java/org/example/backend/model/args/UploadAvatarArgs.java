package org.example.backend.model.args;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadAvatarArgs {
    private Long userId;
    private String fileName;
    private String mimeType;
    private Long fileSize;
}
