package org.example.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class GlobalFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final String AUTH_TOKEN_KEY = "auth:token:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 精确匹配
        if (pathMatcher.match("/api/auth/login", request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 模式匹配
        if (pathMatcher.match("/api/enterprise/**", request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Header校验
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        try {
            // Token校验
            String token = header.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                return;
            }

            // Redis获取用户信息
            Map<Object, Object> userInfo = redisTemplate.opsForHash().entries(AUTH_TOKEN_KEY + token);
            if (userInfo.isEmpty()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                return;
            }
            Long userId = Long.valueOf((String) userInfo.get("userId"));
            String userName = String.valueOf(userInfo.get("userName"));
            @SuppressWarnings("unchecked") List<String> roles = (List<String>) userInfo.get("roles");

            // 构建认证信息
            UserDetails userDetails = new GlobalUserDetails(userId, userName, null, roles, token);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
