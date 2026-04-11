package org.example.backend.model.request.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MemberCreationReq {
    @NotBlank
    private String username;
    @NotNull
    @PositiveOrZero
    private Long nodeId;
    @NotNull
    @PositiveOrZero
    private Long roleId;
}
