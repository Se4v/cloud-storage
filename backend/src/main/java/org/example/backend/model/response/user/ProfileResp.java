package org.example.backend.model.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResp {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String mobile;
    private String email;
}
