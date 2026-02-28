package org.example.backend.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class GlobalUserDetails implements UserDetails {
    private final Long userId;                  // 用户ID
    private final String username;              // 用户名
    private final String password;              // 密码
    private final List<String> roles;           // 用户角色
    private final String token;                 // JWT令牌

    public GlobalUserDetails(Long userId,
                             String username,
                             String password,
                             List<String> roles,
                             String token) {
        this.userId = userId;
        this.username = username;
        this.password = password == null ? "" : password;
        this.roles = roles == null ? new ArrayList<>() : roles;
        this.token = token == null ? "" : token;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
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
