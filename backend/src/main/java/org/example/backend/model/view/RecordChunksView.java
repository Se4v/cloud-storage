package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecordChunksView {
    private List<View> viewList;

    @Data
    @Builder
    public static class View {
        private String uploadId;
        private String chunkNumber;
        private Boolean success;
    }
}
