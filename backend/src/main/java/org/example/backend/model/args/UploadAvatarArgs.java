package org.example.backend.model.args;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadAvatarArgs {
    private MultipartFile file;
    private String fileName;
    private String mimeType;
    private Long fileSize;
}
