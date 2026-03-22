package org.example.backend.model.args;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeleteNodeArgs {
    @NotNull
    private List<Long> nodeIds;
}
