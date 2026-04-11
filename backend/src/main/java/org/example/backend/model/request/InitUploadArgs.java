package org.example.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class InitUploadArgs {
    @NotNull
    private Long driveId;
    @NotNull
    private Long parentId;
    @NotEmpty
    private List<Arg> argList;

    @Data
    public static class Arg {
        @NotBlank
        private String entryName;
        @NotBlank
        private String sha256;
        @PositiveOrZero
        private Long fileSize;
        private Integer totalChunks;
        private String mimeType;
    }
}
