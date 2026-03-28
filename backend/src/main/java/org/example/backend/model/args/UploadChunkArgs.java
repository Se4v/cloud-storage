package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UploadChunkArgs {
    @NotEmpty
    private List<Arg> argList;

    @Data
    public static class Arg {
        @NotBlank
        private String sha256;
        @NotBlank
        private String chunkNumber;
        @NotBlank
        private String etag;
        private Integer chunkSize;
    }
}
