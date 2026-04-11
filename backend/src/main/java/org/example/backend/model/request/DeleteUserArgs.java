package org.example.backend.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteUserArgs {
    @NotEmpty
    private List<Long> userIds;
}
