package org.example.backend.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class OrgUserDetails implements UserDetails {
    private final Long userId;                  // 用户ID
    private final String username;              // 用户名
    private final Long nodeId;                  // 组织节点ID
    private final String role;                  // 组织内角色
    private final List<String> permissions;     // 组织内权限

    public OrgUserDetails(Long userId, String username, Long nodeId, String role, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.nodeId = nodeId;
        this.role = role;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(
                Stream.of(role).map(r -> new SimpleGrantedAuthority("ROLE_" + r)),
                permissions.stream().map(permission -> new SimpleGrantedAuthority(permission))
        ).toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
