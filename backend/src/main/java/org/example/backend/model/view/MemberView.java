package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberView {
    private Long id;
    private String username;
    private String realName;
    private String nodeName;
    private String roleName;
}
