package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeleteEntryArgs {
    @NotNull
    private Long driveId;
    @NotNull
    private List<Long> ids;
}
