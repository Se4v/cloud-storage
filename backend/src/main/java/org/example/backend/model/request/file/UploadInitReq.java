package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class UploadInitReq {
    @NotNull
    private Long driveId;
    @NotNull
    private Long parentId;
    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotBlank
        private String entryName;
        @NotBlank
        private String sha256;
        @PositiveOrZero
        private Long fileSize;
        private Integer totalChunks;
        private String mimeType;
    }
}
