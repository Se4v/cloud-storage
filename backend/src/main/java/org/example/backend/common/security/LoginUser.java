package org.example.backend.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LoginUser {
    private Long userId;                                // 用户ID
    private String username;                            // 用户名
    private String realName;                            // 用户真实姓名
    private List<String> systemRoles;                   // 系统角色
    private List<String> systemPermissions;             // 系统角色权限
    private Map<Long, List<String>> orgRoles;           // 组织角色
    private Map<Long, List<String>> orgPermissions;     // 组织角色权限

    @JsonIgnore
    private String token;
    @JsonIgnore
    private Long orgId;
    @JsonIgnore
    private boolean superAdmin = false;
    @JsonIgnore
    private List<Long> manageNodeIds = List.of();
}
