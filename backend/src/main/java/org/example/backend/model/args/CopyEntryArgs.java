package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CopyEntryArgs {
    @NotNull
    private Long id;
    @NotNull
    private Long driveId;
    @NotNull
    private Long targetId;
}
