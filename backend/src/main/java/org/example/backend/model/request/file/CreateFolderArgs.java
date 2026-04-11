package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateFolderArgs {
    @NotNull
    private Long driveId;
    @NotNull
    @PositiveOrZero
    private Long parentId;
    @NotBlank
    private String folderName;
}
