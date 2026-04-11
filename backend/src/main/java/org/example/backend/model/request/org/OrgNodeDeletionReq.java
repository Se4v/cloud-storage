package org.example.backend.model.request.org;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrgNodeDeletionReq {
    @NotEmpty
    private List<Long> nodeIds;
}
