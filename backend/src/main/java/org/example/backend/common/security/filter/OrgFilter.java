package org.example.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.security.OrgUserDetails;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Component
public class OrgFilter extends OncePerRequestFilter {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final String AUTH_TOKEN_KEY = "auth:token:";
    private static final String ORG_ROLE_KEY = "org:role:";
    private static final String ROLE_KEY = "role:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 只处理企业空间接口
        if (!pathMatcher.match("/api/enterprise/**", request.getRequestURI())) {
            filterChain.doFilter(request, response);
        }

        // 校验header
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        try {
            // 获取token并解析
            String token = header.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                return;
            }

            // 获取当前用户信息
            if (!redisTemplate.hasKey(AUTH_TOKEN_KEY + token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                return;
            };
            Map<Object, Object> userInfo = redisTemplate.opsForHash().entries(AUTH_TOKEN_KEY + token);

            String userId = String.valueOf(userInfo.get("userId"));
            String username = String.valueOf(userInfo.get("username"));
            String[] parts = request.getRequestURI().split("/");
            String nodeId = parts[3];

            // 获取角色
            String role = String.valueOf(redisTemplate.opsForHash().get(ORG_ROLE_KEY + userId, nodeId));
            if (role == null) {
                // TODO：降级从数据库获取
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "非该部门成员");
                return;
            }

            // 获取权限列表
            Set<String> permissions = redisTemplate.opsForSet().members(ROLE_KEY + role);
            if (permissions == null || permissions.isEmpty()) {
                // TODO：降级从数据库获取
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "<UNK>");
                return;
            }

            // 构建新的Authentication
            UserDetails userDetails = new OrgUserDetails(
                    Long.parseLong(userId),
                    username,
                    Long.parseLong(nodeId),
                    role,
                    new ArrayList<>(permissions)
            );
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
