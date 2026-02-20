package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class InitUploadRequest {
    private List<RequestItem> requestItems;
    private Long userId;
    private Long driveId;
    private Long parentId;

    @Data
    public static class RequestItem {
        private String entryName;
        private String sha256;
        private Long fileSize;
        private Integer chunkSize;
        private Integer totalChunks;
    }
}
