package org.example.backend.common.security;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LoginUser {
    private Long userId;
    private String username;
    private List<String> systemRoles;
    private List<String> systemPermissions;
    private Map<Long, List<String>> orgRoles;
    private Map<Long, List<String>> orgPermissions;
    private String token;
}
