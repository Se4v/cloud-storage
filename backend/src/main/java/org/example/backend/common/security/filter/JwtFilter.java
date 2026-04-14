package org.example.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.constant.RedisConsts;
import org.example.backend.common.security.LoginUser;
import org.example.backend.common.util.JwtUtils;
import org.jetbrains.annotations.NotNull;
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

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_ORG_ID = "X-Org-Id";
    private static final int BEARER_PREFIX_LENGTH = 7;

    public JwtFilter(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 执行过滤器内部逻辑
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws IOException, ServletException {
        // 获取请求关键信息
        String header = request.getHeader(AUTHORIZATION);
        String requestURI = request.getRequestURI();
        String orgId = request.getHeader(X_ORG_ID);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 截取纯Token字符串
        String token = header.substring(BEARER_PREFIX_LENGTH);
        if (!jwtUtils.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效token");
            return;
        }

        // 从Redis中获取登录用户信息
        LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(RedisConsts.KEY_AUTH_USER + token);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效token");
            return;
        }
        loginUser.setToken(token);

        // 判断用户是否为系统超级管理员
        if (loginUser.getSystemRoles() != null && loginUser.getSystemRoles().contains(ROLE_ADMIN)) {
            loginUser.setSuperAdmin(true);
        }

        // 解析用户管辖的部门ID
        List<Long> manageNodeIds = new ArrayList<>();
        if (loginUser.getOrgRoles() != null) {
            manageNodeIds = loginUser.getOrgRoles().entrySet().stream()
                    .filter(entry -> entry.getValue().contains(ROLE_MANAGER))
                    .map(Map.Entry::getKey)
                    .toList();
        }
        loginUser.setManageNodeIds(manageNodeIds);

        // 整合用户权限集合
        Set<String> authorities = new HashSet<>();
        if (loginUser.getSystemPermissions() != null) {
            authorities.addAll(loginUser.getSystemPermissions());
        }

        //  处理企业空间接口
        if (requestURI.startsWith("/api/enterprise")) {
            if (orgId == null || orgId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少 X-Org-Id 请求头");
                return;
            }
            Long currentOrgId = Long.valueOf(orgId);

            // 获取当前企业下的权限，不存在则说明用户无该企业权限
            List<String> orgPermissions = loginUser.getOrgPermissions().get(currentOrgId);
            if (orgPermissions != null) {
                authorities.addAll(orgPermissions);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "您不属于该企业或部门");
                return;
            }
            loginUser.setOrgId(currentOrgId);
        }

        // 封装认证对象
        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 放行请求，执行后续业务逻辑
        filterChain.doFilter(request, response);
    }
}
