package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.executor.BatchResult;
import org.example.backend.common.Result;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.*;
import org.example.backend.model.args.AssignGlobalRoleArgs;
import org.example.backend.model.args.CreateUserArgs;
import org.example.backend.model.args.DeleteUserArgs;
import org.example.backend.model.args.UpdateUserArgs;
import org.example.backend.model.entity.*;
import org.example.backend.model.view.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConfigMapper configMapper;
    @Autowired
    private DriveMapper driveMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;
    private static final int UNDELETED = 0;
    private static final int DELETED = 1;

    /**
     * 创建用户
     */
    @Transactional
    public void createUser(CreateUserArgs args) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getUsername, args.getUsername())
                .eq(User::getDeleted, UNDELETED);
        User existUser = userMapper.selectOne(userQuery);
        if (existUser != null) throw new BusinessException("用户名已存在");

        // 调用配置表
        LambdaQueryWrapper<Config> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(Config::getEnabled, ENABLED)
                .eq(Config::getConfigKey, "default_password");
        Config config = configMapper.selectOne(configQuery);

        // 创建用户
        User user = User.builder()
                .username(args.getUsername())
                .password(passwordEncoder.encode(config.getConfigValue()))
                .realName(args.getRealName())
                .mobile(args.getMobile())
                .build();

        int userCount = userMapper.insert(user);
        if (userCount != 1) throw new BusinessException("创建用户失败");

        Drive drive = Drive.builder()
                .driveName("个人空间")
                .driveType(4)
                .nodeId(0L)
                .userId(user.getId())
                .totalQuota(args.getStorageQuota())
                .usedQuota(0L)
                .build();

        int driveCount = driveMapper.insert(drive);
        if (driveCount != 1) throw new BusinessException("<UNK>");
    }

    /**
     * 批量删除用户（逻辑删除）
     */
    @Transactional
    public void deleteUsers(DeleteUserArgs args) {
        if (args.getUserIds() == null || args.getUserIds().isEmpty()) throw new BusinessException("请选择要删除的用户");

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(User::getId, args.getUserIds()).set(User::getDeleted, DELETED);

        int count = userMapper.update(updateWrapper);
        if (count != args.getUserIds().size()) throw new BusinessException("删除用户失败");
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(UpdateUserArgs args) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getRealName() != null, User::getRealName, args.getRealName())
                .set(args.getMobile() != null, User::getMobile, args.getMobile())
                .set(args.getEmail() != null, User::getEmail, args.getEmail())
                .set(args.getIsEnabled() != null, User::getEnabled, Boolean.TRUE.equals(args.getIsEnabled()) ? ENABLED : DISABLED)
                .eq(User::getId, args.getId());

        int count = userMapper.update(updateWrapper);
        if (count != 1) throw new BusinessException("更新用户失败");
    }

    /**
     * 查询所有未删除的用户
     */
    public List<UserView> listAllUsers() {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDeleted, UNDELETED).orderByDesc(User::getCreatedAt);
        List<User> users = userMapper.selectList(queryWrapper);
        if (users == null || users.isEmpty()) return new ArrayList<>();
        List<Long> userIds = users.stream().map(User::getId).toList();

        // 查询用户配额
        LambdaQueryWrapper<Drive> driveQuery = new LambdaQueryWrapper<>();
        driveQuery.eq(Drive::getDriveType, 4).in(Drive::getUserId, userIds);
        List<Drive> drives = driveMapper.selectList(driveQuery);
        Map<Long, Long> driveMap = drives.stream().collect(Collectors.toMap(Drive::getUserId, Drive::getTotalQuota));

        // 查询用户全局角色
        LambdaQueryWrapper<UserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.in(UserRole::getUserId, userIds);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        Map<Long, List<Long>> userRoleMap = userRoles.stream()
                .collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(UserRole::getRoleId, Collectors.toList())
                ));

        // 转换
        List<UserView> userViews = users.stream()
                .map(user -> UserView.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .mobile(user.getMobile())
                        .email(user.getEmail())
                        .isEnabled(user.getEnabled() == 1)
                        .storageQuota(driveMap.getOrDefault(user.getId(), null))
                        .roles(userRoleMap.getOrDefault(user.getId(), List.of()))
                        .build())
                .toList();

        return userViews;
    }

    // TODO:逻辑需要修改
    @Transactional
    public void assignGlobalRole(AssignGlobalRoleArgs args) {
        List<Long> roleIds = args.getRoleIds();
        List<UserRole> userRoleList = new ArrayList<>();
        for (Long roleId : roleIds) {
            userRoleList.add(UserRole.builder()
                            .userId(args.getId())
                            .roleId(roleId)
                            .build());
        }

        List<BatchResult> results = userRoleMapper.insert(userRoleList);
        int insertCount = results.stream()
                .flatMapToInt(result -> Arrays.stream(result.getUpdateCounts()))
                .sum();
        if (insertCount != roleIds.size()) throw new BusinessException("<UNK>");
    }

    @Transactional
    public void resetPassword(Long userId) {
        // 查询用户
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getId, userId);
        User user = userMapper.selectOne(userQuery);
        if (user == null) throw new BusinessException("<UNK>");

        // 调用配置表
        LambdaQueryWrapper<Config> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(Config::getEnabled, ENABLED)
                .eq(Config::getConfigKey, "default_password");
        Config config = configMapper.selectOne(configQuery);

        // 重置密码
        LambdaUpdateWrapper<User> userUpdate = new LambdaUpdateWrapper<>();
        userUpdate.set(User::getPassword, passwordEncoder.encode(config.getConfigValue()))
                .eq(User::getId, userId);
        int count = userMapper.update(userUpdate);
        if (count != 1) throw new BusinessException("<UNK>");
    }

    public List<Role> listGlobalRole() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getType, 1)
                .eq(Role::getEnabled, ENABLED)
                .eq(Role::getDeleted, UNDELETED);
        return roleMapper.selectList(queryWrapper);
    }
}
