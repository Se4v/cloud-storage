package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ChunkUploadReq {
    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotBlank
        private String sha256;
        @NotBlank
        private String chunkNumber;
        private String etag;
    }
}
