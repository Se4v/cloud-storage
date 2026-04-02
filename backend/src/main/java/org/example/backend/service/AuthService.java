package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.security.LoginUser;
import org.example.backend.common.util.JwtUtil;
import org.example.backend.mapper.*;
import org.example.backend.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JwtUtil jwtUtil;

    private static final String AUTH_USER_KEY = "auth:user:";

    public String login(String username, String password) {
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getUsername, username);
        User user = userMapper.selectOne(userQuery);
        if (user == null || user.getEnabled() == 0) return null;

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Username or password is incorrect");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        List<String> systemRoles = userMapper.selectSystemRoles(user.getId());
        List<String> systemPermissions = userMapper.selectSystemPermissions(user.getId());

        List<Map<String, Object>> orgRoleList = userMapper.selectOrgRoles(user.getId());
        List<Map<String, Object>> orgPermissionList = userMapper.selectOrgPermissions(user.getId());
        Map<Long, List<String>> orgRoles = orgRoleList.stream()
                .collect(Collectors.groupingBy(
                   map -> (Long) map.get("node_id"),
                        Collectors.mapping(
                                map -> (String) map.get("role_code"),
                                Collectors.toList()
                        )
                ));
        Map<Long, List<String>> orgPermissions = orgPermissionList.stream()
                .collect(Collectors.groupingBy(
                        map -> (Long) map.get("node_id"),
                        Collectors.mapping(
                                map -> (String) map.get("perm_code"),
                                Collectors.toList()
                        )
                ));

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setSystemRoles(systemRoles);
        loginUser.setSystemPermissions(systemPermissions);
        loginUser.setOrgRoles(orgRoles);
        loginUser.setOrgPermissions(orgPermissions);

        String key = AUTH_USER_KEY + token;
        redisTemplate.expire(AUTH_USER_KEY + token, 3, TimeUnit.HOURS);

        return token;
    }

    public void logout(String token) {
        redisTemplate.delete(AUTH_USER_KEY + token);
        SecurityContextHolder.clearContext();
    }
}
