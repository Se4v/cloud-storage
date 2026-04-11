package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class MoveEntryArgs {
    @NotNull
    private List<Long> ids;
    @NotNull
    private Long driveId;
    @NotNull
    @PositiveOrZero
    private Long targetId;
}
