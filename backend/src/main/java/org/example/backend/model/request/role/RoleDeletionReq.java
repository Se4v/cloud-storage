package org.example.backend.model.request.role;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RoleDeletionReq {
    @NotEmpty
    private List<Long> roleIds;
}
