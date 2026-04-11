package org.example.backend.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateMemberArgs {
    @NotNull
    @PositiveOrZero
    private Long memberId;
    @NotNull
    @PositiveOrZero
    private Long nodeId;
    @NotNull
    @PositiveOrZero
    private Long roleId;
}
