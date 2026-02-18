package org.example.backend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // uri校验
        String uri = request.getRequestURI();
        if (uri.equals("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Header校验
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            sendResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token无效", null);
            return;
        }

        try {
            // Token校验
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                sendResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token无效", null);
                return;
            }

            // Redis获取用户信息
            String key = "auth:token:" + token;
            Map<Object, Object> userInfo = redisTemplate.opsForHash().entries(key);
            if (userInfo.isEmpty()) {
                sendResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token无效", null);
                return;
            }
            Long userId = (Long) userInfo.get("userId");
            String userName = (String) userInfo.get("userName");
            @SuppressWarnings("unchecked") List<String> roles = (List<String>) userInfo.get("roles");
            @SuppressWarnings("unchecked") List<String> permissions = (List<String>) userInfo.get("permissions");

            // 构建认证信息
            UserDetails userDetails = new MyUserDetails(userId, userName, null, roles, permissions, token);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            sendResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token无效", null);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendResponse(HttpServletResponse response,
                              int code,
                              String msg,
                              Object data) throws IOException {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("msg", msg);
        body.put("data", data);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
