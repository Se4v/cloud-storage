package org.example.backend.model.args;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DirectUploadArgs {
    private Long driveId;
    private Long parentId;
    private Long userId;

    private MultipartFile file;
    private String entryName;
    private String sha256;
    private String mimeType;
    private Long fileSize;
}
