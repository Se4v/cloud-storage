package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class InitUploadArgs {
    private List<Arg> argList;
    private Long userId;
    private Long driveId;
    private Long parentId;

    @Data
    public static class Arg {
        private String entryName;
        private String sha256;
        private Long fileSize;
        private Integer chunkSize;
        private Integer totalChunks;
        private String mimeType;
    }
}
