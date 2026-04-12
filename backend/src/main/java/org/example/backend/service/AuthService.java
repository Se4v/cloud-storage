package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.security.LoginUser;
import org.example.backend.common.util.JwtUtil;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.mapper.*;
import org.example.backend.model.entity.*;
import org.example.backend.model.request.auth.AuthReq;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String TYPE_LOGIN_ADMIN = "admin";
    private static final String KEY_AUTH_USER = "auth:user:";
    private static final String KEY_AUTH_TOKEN = "auth:token:";

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder,
                       RedisTemplate<String, Object> redisTemplate, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public String login(AuthReq req) {
        // 防止重复登录
        String existingToken = (String) redisTemplate.opsForValue().get(KEY_AUTH_USER + req.getUsername());
        if (existingToken != null) {
            return existingToken;
        }

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, req.getUsername()));
        if (user == null || user.getEnabled() == 0) return null;

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("Username or password is incorrect");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        List<String> systemRoles = userMapper.selectSystemRoles(user.getId());
        List<String> systemPermissions = userMapper.selectSystemPermissions(user.getId());

        if (req.getLoginType().equals(TYPE_LOGIN_ADMIN)) {
            if(!systemRoles.contains(ROLE_ADMIN)) throw new BusinessException("<UNK>");
        }

        List<Map<String, Object>> orgRoleList = userMapper.selectOrgRoles(user.getId());
        List<Map<String, Object>> orgPermissionList = userMapper.selectOrgPermissions(user.getId());
        Map<Long, List<String>> orgRoles = orgRoleList.stream()
                .collect(Collectors.groupingBy(
                   map -> ((Number) map.get("node_id")).longValue(),
                        Collectors.mapping(map -> (String) map.get("role_code"), Collectors.toList())
                ));
        Map<Long, List<String>> orgPermissions = orgPermissionList.stream()
                .collect(Collectors.groupingBy(
                        map -> ((Number) map.get("node_id")).longValue(),
                        Collectors.mapping(map -> (String) map.get("perm_code"), Collectors.toList())
                ));

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setSystemRoles(systemRoles);
        loginUser.setSystemPermissions(systemPermissions);
        loginUser.setOrgRoles(orgRoles);
        loginUser.setOrgPermissions(orgPermissions);

        // TODO:使用 Redis 事务或 Pipeline 保证两个写操作的原子性
        redisTemplate.opsForValue().set(KEY_AUTH_USER + token, loginUser, 4, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(KEY_AUTH_TOKEN + req.getUsername(), token, 4, TimeUnit.HOURS);

        return token;
    }

    public void logout() {
        String tokenKey = KEY_AUTH_USER + SecurityUtil.getToken();
        String usernameKey = KEY_AUTH_TOKEN + SecurityUtil.getUsername();
        redisTemplate.delete(Arrays.asList(tokenKey, usernameKey));
        SecurityContextHolder.clearContext();
    }
}
