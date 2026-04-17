package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.constant.RedisConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.security.LoginUser;
import org.example.backend.common.util.JwtUtils;
import org.example.backend.common.util.SecurityUtils;
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
    private final JwtUtils jwtUtils;

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String TYPE_LOGIN_ADMIN = "admin";

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder,
                       RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 用户登录
     * @param req 登录请求参数
     * @return 登录成功生成的JWT Token，失败返回null
     */
    public String login(AuthReq req) {
        // 防止重复登录
        String existingToken = (String) redisTemplate.opsForValue().get(RedisConsts.KEY_AUTH_TOKEN + req.getUsername());
        if (existingToken != null) {
            return existingToken;
        }

        // 根据用户名查询数据库用户信息
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, req.getUsername()));
        if (user == null || user.getEnabled() == DbConsts.ENABLED_NO) return null;
        // 密码校验
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 查询用户的系统全局角色、权限
        List<String> systemRoles = userMapper.selectSystemRoles(user.getId());
        List<String> systemPermissions = userMapper.selectSystemPermissions(user.getId());

        // 管理员登录校验
        if (req.getLoginType().equals(TYPE_LOGIN_ADMIN)) {
            if(!systemRoles.contains(ROLE_ADMIN)) throw new BusinessException("您无权限访问该后台功能");
        }

        // 查询用户组织的角色、权限
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

        // 封装登录用户完整信息
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setSystemRoles(systemRoles);
        loginUser.setSystemPermissions(systemPermissions);
        loginUser.setOrgRoles(orgRoles);
        loginUser.setOrgPermissions(orgPermissions);

        // Redis缓存登录信息
        // TODO:使用 Redis 事务或 Pipeline 保证两个写操作的原子性
        redisTemplate.opsForValue().set(RedisConsts.KEY_AUTH_USER + token, loginUser, 4, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(RedisConsts.KEY_AUTH_TOKEN + req.getUsername(), token, 4, TimeUnit.HOURS);

        return token;
    }

    /**
     * 用户登出
     */
    public void logout() {
        String tokenKey = RedisConsts.KEY_AUTH_USER + SecurityUtils.getToken();
        String usernameKey = RedisConsts.KEY_AUTH_TOKEN + SecurityUtils.getUsername();
        redisTemplate.delete(Arrays.asList(tokenKey, usernameKey));
        SecurityContextHolder.clearContext();
    }
}
