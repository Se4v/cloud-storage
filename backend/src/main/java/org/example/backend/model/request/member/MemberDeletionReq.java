package org.example.backend.model.request.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MemberDeletionReq {
    @NotEmpty
    private List<Long> memberIds;
}
