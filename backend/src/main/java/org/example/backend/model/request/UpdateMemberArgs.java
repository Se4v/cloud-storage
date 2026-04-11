package org.example.backend.model.request;

import lombok.Data;

@Data
public class UpdateMemberArgs {
    private Long memberId;
    private Long nodeId;
    private Long roleId;
}
