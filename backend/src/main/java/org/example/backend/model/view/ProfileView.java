package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileView {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String mobile;
    private String email;
}
