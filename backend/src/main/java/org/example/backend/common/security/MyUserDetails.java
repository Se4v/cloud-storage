package org.example.backend.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyUserDetails implements UserDetails {
    private final Long userId;                  // 用户ID
    private final String username;              // 用户名
    private final String password;              // 密码
    private final List<String> roles;           // 用户角色
    private final List<String> permissions;     // 用户权限
    private final String token;                 // JWT令牌

    public MyUserDetails(Long userId,
                             String username,
                             String password,
                             List<String> roles,
                             List<String> permission,
                             String token) {
        this.userId = userId;
        this.username = username;
        this.password = password == null ? "" : password;
        this.roles = roles == null ? new ArrayList<>() : roles;
        this.permissions = permission == null ? new ArrayList<>() : permission;
        this.token = token == null ? "" : token;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getToken() {
        return token;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)),
                permissions.stream().map(permission -> new SimpleGrantedAuthority(permission))
        ).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
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
