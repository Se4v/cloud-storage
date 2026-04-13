package org.example.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.constant.RedisConsts;
import org.example.backend.common.security.LoginUser;
import org.example.backend.common.util.JwtUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    public JwtFilter(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        String orgId = request.getHeader("X-Org-Id");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (!jwtUtils.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized2");
            return;
        }

        LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(RedisConsts.KEY_AUTH_USER + token);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized3");
            return;
        }
        loginUser.setToken(token);

        // 判断是否是超级管理员
        if (loginUser.getSystemRoles() != null && loginUser.getSystemRoles().contains(ROLE_ADMIN)) {
            loginUser.setSuperAdmin(true);
        }

        // 获取自己管辖的部门
        List<Long> manageNodeIds = new ArrayList<>();
        if (loginUser.getOrgRoles() != null) {
            manageNodeIds = loginUser.getOrgRoles().entrySet().stream()
                    .filter(entry -> entry.getValue().contains(ROLE_MANAGER)) // 1. 筛选包含管理员角色的条目
                    .map(Map.Entry::getKey)                                  // 2. 提取节点 ID
                    .toList();
        }
        loginUser.setManageNodeIds(manageNodeIds);

        Set<String> authorities = new HashSet<>();
        if (loginUser.getSystemPermissions() != null) {
            authorities.addAll(loginUser.getSystemPermissions());
        }

        // 判断是企业空间还是个人空间
        if (requestURI.startsWith("/api/enterprise")) {
            // 访问企业空间
            if (orgId == null || orgId.isEmpty()) {
                // 拒绝服务：访问企业接口必须指定企业上下文！
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少 X-Org-Id 请求头");
                return;
            }
            Long currentOrgId = Long.valueOf(orgId);

            List<String> orgPermissions = loginUser.getOrgPermissions().get(currentOrgId);
            if (orgPermissions != null) {
                authorities.addAll(orgPermissions); // 比如注入了 "file:delete"
            } else {
                // 用户不属于该企业，直接抛异常，防越权访问
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您不属于该企业或部门");
                return;
            }
            loginUser.setOrgId(currentOrgId);
        }

        // 封装 Authentication 并放行
        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
