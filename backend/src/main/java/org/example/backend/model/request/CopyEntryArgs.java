package org.example.backend.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CopyEntryArgs {
    @NotNull
    private Long id;
    @NotNull
    private Long driveId;
    @NotNull
    @PositiveOrZero
    private Long targetId;
}
