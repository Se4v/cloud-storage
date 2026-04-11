package org.example.backend.model.request.member;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MemberUpdateReq {
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
