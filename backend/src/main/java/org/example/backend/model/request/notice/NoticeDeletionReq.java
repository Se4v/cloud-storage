package org.example.backend.model.request.notice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class NoticeDeletionReq {
    @NotNull
    private List<Long> ids;
}
