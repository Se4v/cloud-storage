package org.example.backend.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteNodeArgs {
    @NotEmpty
    private List<Long> nodeIds;
}
