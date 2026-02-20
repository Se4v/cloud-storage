package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class RecordChunksRequest {
    private List<RequestItem> requestItems;

    @Data
    public static class RequestItem {
        private String uploadId;
        private String chunkNumber;
        private String etag;
        private Integer chunkSize;
    }
}
