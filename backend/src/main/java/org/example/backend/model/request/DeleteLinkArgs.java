package org.example.backend.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeleteLinkArgs {
    @NotNull
    private List<Long> linkIds;
}
