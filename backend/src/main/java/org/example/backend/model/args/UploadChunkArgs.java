package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class UploadChunkArgs {
    private List<Arg> argList;

    @Data
    public static class Arg {
        private String sha256;
        private String chunkNumber;
        private String etag;
        private Integer chunkSize;
    }
}
