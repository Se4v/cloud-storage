package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InitUploadResponse {
    private List<ResponseItem> responseItems;
    private Integer totalCount;
    private Integer successCount;

    @Data
    @Builder
    public static class ResponseItem {
        private String entryName;
        private Boolean success;
        private String message;

        private Boolean skipUpload;
        // 以下字段在skipUpload=false和success=true时有效
        private Boolean directUpload;
        private String sha256;
        private String uploadId;
        private List<Integer> uploadedChunks;
        private List<String> chunkUrls;
    }
}
