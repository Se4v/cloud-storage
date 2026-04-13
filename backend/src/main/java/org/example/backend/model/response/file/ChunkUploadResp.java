package org.example.backend.model.response.file;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChunkUploadResp {
    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        private String uploadId;
        private String chunkNumber;
        private Boolean success;
    }
}
