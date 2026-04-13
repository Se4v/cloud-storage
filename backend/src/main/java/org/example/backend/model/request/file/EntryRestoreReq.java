package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EntryRestoreReq {
    @NotNull
    private List<Long> ids;
}
