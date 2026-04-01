package org.example.backend.model.args;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RenameEntryArgs {
    @NotNull
    private Long id;
    @NotNull
    private Long driveId;
    @NotBlank
    private String newEntryName;
}
