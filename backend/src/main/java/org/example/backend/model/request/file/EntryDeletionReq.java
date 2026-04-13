package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EntryDeletionReq {
    @NotNull
    private Long driveId;
    @NotNull
    private List<Long> ids;
}
