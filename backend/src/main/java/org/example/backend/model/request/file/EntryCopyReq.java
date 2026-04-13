package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class EntryCopyReq {
    @NotNull
    private Long id;
    @NotNull
    private Long driveId;
    @NotNull
    @PositiveOrZero
    private Long targetId;
}
