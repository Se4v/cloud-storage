package org.example.backend.model.request.share;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LinkDeletionReq {
    @NotNull
    private List<Long> linkIds;
}
