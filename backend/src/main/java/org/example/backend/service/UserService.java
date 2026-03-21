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

import java.util.*;
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
        configQuery.eq(Config::getIsEnabled, ENABLED)
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

    @Transactional
    public void assignGlobalRole(AssignGlobalRoleArgs args) {
        if (args == null) throw new BusinessException("角色分配参数不能为空");
        Long userId = args.getUserId();
        List<Long> targetRoleIds = args.getRoleIds() == null ? List.of() : args.getRoleIds() ;

        // 查询该用户当前已绑定的角色
        LambdaQueryWrapper<UserRole> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(UserRole::getUserId, userId);
        List<UserRole> existingUserRoles = userRoleMapper.selectList(userQuery);

        // 提取已有的角色ID集合
        Set<Long> existingRoleIds = existingUserRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());
        // 目标角色ID集合
        Set<Long> targetRoleIdSet = targetRoleIds.stream()
                .filter(id -> id != null && id > 0) // 过滤无效ID
                .collect(Collectors.toSet());

        // 计算需要新增的角色
        Set<Long> addRoleIds = targetRoleIdSet.stream()
                .filter(id -> !existingRoleIds.contains(id))
                .collect(Collectors.toSet());
        // 计算需要删除的角色
        Set<Long> deleteRoleIds = existingRoleIds.stream()
                .filter(id -> !targetRoleIdSet.contains(id))
                .collect(Collectors.toSet());

        // 新增角色关联
        if (!addRoleIds.isEmpty()) {
            List<UserRole> addUserRoles = addRoleIds.stream()
                    .map(roleId -> UserRole.builder()
                            .userId(userId)
                            .roleId(roleId)
                            .build())
                    .toList();
            List<BatchResult> results = userRoleMapper.insert(addUserRoles);
            int insertCount = results.stream()
                    .flatMapToInt(result -> Arrays.stream(result.getUpdateCounts()))
                    .sum();
            if (insertCount != addUserRoles.size()) throw new BusinessException("<UNK>");
        }

        // 删除角色关联
        if (!deleteRoleIds.isEmpty()) {
            LambdaQueryWrapper<UserRole> deleteQuery = new LambdaQueryWrapper<>();
            deleteQuery.eq(UserRole::getUserId, userId).in(UserRole::getRoleId, deleteRoleIds);
            int deleteCount = userRoleMapper.delete(deleteQuery);
            if (deleteCount != deleteRoleIds.size()) throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void resetPassword(Long userId) {
        // 查询用户
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .eq(User::getEnabled, 1);
        User user = userMapper.selectOne(userQuery);
        if (user == null) throw new BusinessException("<UNK>");

        // 调用配置表
        LambdaQueryWrapper<Config> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(Config::getIsEnabled, ENABLED)
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
