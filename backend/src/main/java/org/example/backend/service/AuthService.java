package org.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.security.MyUserDetails;
import org.example.backend.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    private static final String AUTH_USER_KEY = "auth:user:";

    public String login(String username, String password) {
        MyUserDetails userDetails = null;
        try {
            userDetails = (MyUserDetails) userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new BusinessException("");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BusinessException("<UNK>");
        }

        String token = jwtUtil.generateToken(userDetails.getUserId(), userDetails.getUsername());

        String key = AUTH_USER_KEY + token;
        redisTemplate.opsForHash().putAll(key, Map.of(
                "userId", userDetails.getUserId(),
                "username", userDetails.getUsername(),
                "roles", userDetails.getRoles(),
                "permissions", userDetails.getPermissions()
        ));
        redisTemplate.expire(AUTH_USER_KEY + token, 3, TimeUnit.HOURS);

        return token;
    }

    public void logout(String token) {
        redisTemplate.delete(AUTH_USER_KEY + token);
        SecurityContextHolder.clearContext();
    }
}
