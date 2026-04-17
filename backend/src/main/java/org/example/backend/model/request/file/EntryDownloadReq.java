package org.example.backend.model.request.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EntryDownloadReq {
    @NotEmpty
    private List<Long> ids;
}