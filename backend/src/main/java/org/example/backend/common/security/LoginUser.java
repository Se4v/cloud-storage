package org.example.backend.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private String token;
    @JsonIgnore
    private Long orgId;
    @JsonIgnore
    private boolean superAdmin = false; // 是否是超级管理员
    @JsonIgnore
    private List<Long> manageNodeIds = List.of(); // 作为部门管理员，他能管理的部门ID集合
}
