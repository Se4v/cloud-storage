package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecordChunksResponse {
    private List<ResponseItem> responseItems;

    @Data
    @Builder
    public static class ResponseItem {
        private String uploadId;
        private String chunkNumber;
        private Boolean success;
    }
}
