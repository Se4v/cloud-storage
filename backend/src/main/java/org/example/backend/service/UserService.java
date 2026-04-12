package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.mapper.*;
import org.example.backend.model.request.role.SystemRoleAssignmentReq;
import org.example.backend.model.request.user.PasswordResetReq;
import org.example.backend.model.request.user.UserCreationReq;
import org.example.backend.model.request.user.UserDeletionReq;
import org.example.backend.model.request.user.UserUpdateReq;
import org.example.backend.model.entity.*;
import org.example.backend.model.response.user.UserView;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final MemberMapper memberMapper;
    private final ConfigMapper configMapper;
    private final DriveMapper driveMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, MemberMapper memberMapper, ConfigMapper configMapper,
                       DriveMapper driveMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.configMapper = configMapper;
        this.driveMapper = driveMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private static final int ENABLED = 1;
    private static final int DISABLED = 0;
    private static final int UNDELETED = 0;
    private static final int DELETED = 1;

    /**
     * 创建用户
     */
    @Transactional
    public void createUser(UserCreationReq req) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, req.getUsername())
                        .eq(User::getDeleted, UNDELETED));
        if (existUser != null) throw new BusinessException("用户名已存在");

        // 调用配置表
        Config config = configMapper.selectOne(
                Wrappers.<Config>lambdaQuery()
                        .eq(Config::getConfigKey, "default_password")
                        .eq(Config::getIsEnabled, ENABLED));

        // 创建用户
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(config.getConfigValue()))
                .realName(req.getRealName())
                .mobile(req.getMobile())
                .build();

        int userCount = userMapper.insert(user);
        if (userCount != 1) throw new BusinessException("创建用户失败");

        Drive drive = Drive.builder()
                .driveName("个人空间")
                .driveType(1)
                .nodeId(0L)
                .userId(user.getId())
                .totalQuota(req.getStorageQuota())
                .usedQuota(0L)
                .build();

        int driveCount = driveMapper.insert(drive);
        if (driveCount != 1) throw new BusinessException("<UNK>");
    }

    /**
     * 批量删除用户（逻辑删除）
     */
    @Transactional
    public void deleteUsers(UserDeletionReq req) {
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getDeleted, DELETED)
                        .in(User::getId, req.getUserIds()));
        if (count != req.getUserIds().size()) throw new BusinessException("删除用户失败");
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(UserUpdateReq req) {
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getRealName, req.getRealName())
                        .set(User::getMobile, req.getMobile())
                        .set(User::getEmail, req.getEmail())
                        .set(User::getEnabled, Boolean.TRUE.equals(req.getIsEnabled()) ? ENABLED : DISABLED)
                        .eq(User::getId, req.getId()));
        if (count != 1) throw new BusinessException("更新用户失败");
    }

    /**
     * 查询所有未删除的用户
     */
    public List<UserView> listAllUsers() {
        // 筛选部门管理查看的数据
        List<Long> manageUserIds = null;
        if (!SecurityUtil.isSuperAdmin()) {
            List<Long> manageNodeIds = SecurityUtil.getManageNodeIds();
            if (manageNodeIds.isEmpty()) return List.of();
            List<Member> members = memberMapper.selectList(
                    Wrappers.<Member>lambdaQuery().in(Member::getNodeId, manageNodeIds));
            manageUserIds = members.stream().map(Member::getNodeId).toList();
        }

        // 查询用户
        List<User> users = userMapper.selectList(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getDeleted, UNDELETED)
                        .in(manageUserIds != null && !manageUserIds.isEmpty(), User::getId, manageUserIds));
        if (users == null || users.isEmpty()) return List.of();
        List<Long> validUserIds = users.stream().map(User::getId).toList();

        // 查询用户配额
        List<Drive> drives = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getDriveType, 1)
                        .in(Drive::getUserId, validUserIds));
        Map<Long, Long> driveMap = drives.stream()
                .collect(Collectors.toMap(Drive::getUserId, Drive::getTotalQuota));

        // 查询用户全局角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                Wrappers.<UserRole>lambdaQuery()
                        .in(UserRole::getUserId, validUserIds));
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
                        .isEnabled(user.getEnabled())
                        .storageQuota(driveMap.getOrDefault(user.getId(), null))
                        .roles(userRoleMap.getOrDefault(user.getId(), List.of()))
                        .build())
                .toList();

        return userViews;
    }

    @Transactional
    public void assignSystemRole(SystemRoleAssignmentReq req) {
        if (req == null) throw new BusinessException("角色分配参数不能为空");
        Long userId = req.getUserId();
        List<Long> targetRoleIds = req.getRoleIds() == null ? List.of() : req.getRoleIds() ;

        // 查询该用户当前已绑定的角色
        List<UserRole> existingUserRoles = userRoleMapper.selectList(
                Wrappers.<UserRole>lambdaQuery()
                        .eq(UserRole::getUserId, userId));

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
            int deleteCount = userRoleMapper.delete(
                    Wrappers.<UserRole>lambdaQuery()
                            .eq(UserRole::getUserId, userId)
                            .in(UserRole::getRoleId, deleteRoleIds));
            if (deleteCount != deleteRoleIds.size()) throw new BusinessException("<UNK>");
        }
    }

    @Transactional
    public void resetPassword(PasswordResetReq req) {
        // 查询用户
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getId, req.getUserId())
                        .eq(User::getDeleted, 0)
                        .eq(User::getEnabled, 1));
        if (user == null) throw new BusinessException("<UNK>");

        // 调用配置表
        Config config = configMapper.selectOne(
                Wrappers.<Config>lambdaQuery()
                        .eq(Config::getConfigKey, "default_password")
                        .eq(Config::getIsEnabled, ENABLED));

        // 重置密码
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getPassword, passwordEncoder.encode(config.getConfigValue()))
                        .eq(User::getId, req.getUserId()));
        if (count != 1) throw new BusinessException("<UNK>");
    }

    public List<Role> listSystemRole() {
        List<Role> systemRoles = roleMapper.selectList(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getType, 1)
                        .eq(Role::getEnabled, ENABLED)
                        .eq(Role::getDeleted, UNDELETED));
        return systemRoles;
    }
}
