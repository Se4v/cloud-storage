package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserView {
    private String id;
    private String username;
    private String realName;
    private String mobile;
    private String email;
    private String status;
    private String storageQuota;
    private List<Long> roles;
}
