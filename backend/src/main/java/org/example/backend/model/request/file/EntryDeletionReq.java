package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EntryDeletionReq {
    @NotNull
    private Long driveId;
    @NotEmpty
    private List<Long> ids;
}
