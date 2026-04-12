package org.example.backend.model.response.member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberRoleView {
    private Long id;
    private String name;
}
