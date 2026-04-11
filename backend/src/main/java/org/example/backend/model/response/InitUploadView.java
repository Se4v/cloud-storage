package org.example.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InitUploadView {
    private List<View> viewList;
    private Integer totalCount;
    private Integer successCount;

    @Data
    @Builder
    public static class View {
        private String entryName;
        private Boolean success;
        private String message;

        private Boolean isSkip;
        private Boolean isMultipart;
        private String sha256;
        private String uploadUrl;
        private List<Integer> uploadedChunks;
        private List<String> chunkUrls;
    }
}
