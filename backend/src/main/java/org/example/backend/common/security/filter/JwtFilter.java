package org.example.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.security.LoginUser;
import org.example.backend.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_AUTH_USER = "auth:user:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        String currentOrgId =  request.getHeader("X-Org-Id");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        String token = header.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        // TODO: 反序列化可能有问题
        LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(KEY_AUTH_USER + token);
        if (loginUser == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        Set<String> authorities = new HashSet<>();
        if (loginUser.getSystemPermissions() != null) {
            authorities.addAll(loginUser.getSystemPermissions());
        }
        // 判断是企业空间还是个人空间
        if (requestURI.startsWith("/api/enterprise")) {
            // 访问企业空间
            if (currentOrgId == null || currentOrgId.isEmpty()) {
                // 拒绝服务：访问企业接口必须指定企业上下文！
                throw new RuntimeException("缺少 X-Org-Id 请求头");
            }

            List<String> orgPermissions = loginUser.getOrgPermissions().get(currentOrgId);
            if (orgPermissions != null) {
                authorities.addAll(orgPermissions); // 比如注入了 "file:delete"
            } else {
                // 用户不属于该企业，直接抛异常，防越权访问
                throw new AccessDeniedException("您不属于该企业或部门");
            }
        }
        // 访问个人空间

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
