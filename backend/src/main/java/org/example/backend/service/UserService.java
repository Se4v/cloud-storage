package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.common.util.SecurityUtils;
import org.example.backend.mapper.*;
import org.example.backend.model.request.role.SystemRoleAssignmentReq;
import org.example.backend.model.request.user.PasswordResetReq;
import org.example.backend.model.request.user.UserCreationReq;
import org.example.backend.model.request.user.UserDeletionReq;
import org.example.backend.model.request.user.UserUpdateReq;
import org.example.backend.model.entity.*;
import org.example.backend.model.response.user.UserResp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final MemberMapper memberMapper;
    private final DriveMapper driveMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final ConfigService configService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, MemberMapper memberMapper, DriveMapper driveMapper,
                       RoleMapper roleMapper, UserRoleMapper userRoleMapper, ConfigService configService,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.driveMapper = driveMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.configService = configService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建新用户
     * @param req 用户创建请求
     */
    @Transactional
    public void createUser(UserCreationReq req) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, req.getUsername())
                        .eq(User::getDeleted, DbConsts.DELETED_NO));
        if (existUser != null) throw new BusinessException("用户名已存在");

        // 创建用户
        String defaultPassword = configService.getDefaultPassword();
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(defaultPassword))
                .realName(req.getRealName())
                .mobile(req.getMobile())
                .build();

        int userCount = userMapper.insert(user);
        if (userCount != 1) throw new BusinessException("创建用户失败");

        Drive drive = Drive.builder()
                .driveName("个人空间")
                .driveType(DbConsts.DRIVE_TYPE_PERSONAL)
                .nodeId(0L)
                .userId(user.getId())
                .totalQuota(req.getStorageQuota())
                .usedQuota(0L)
                .build();

        int driveCount = driveMapper.insert(drive);
        if (driveCount != 1) throw new BusinessException("创建用户失败");
    }

    /**
     * 删除用户
     * @param req 用户删除请求
     */
    @Transactional
    public void deleteUsers(UserDeletionReq req) {
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getDeleted, DbConsts.DELETED_YES)
                        .in(User::getId, req.getUserIds()));
        if (count != req.getUserIds().size()) throw new BusinessException("删除用户失败");
    }

    /**
     * 更新用户信息
     * @param req 用户更新请求
     */
    @Transactional
    public void updateUser(UserUpdateReq req) {
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getRealName, req.getRealName())
                        .set(User::getMobile, req.getMobile())
                        .set(User::getEmail, req.getEmail())
                        .set(User::getEnabled, Boolean.TRUE.equals(req.getIsEnabled()) ? DbConsts.ENABLED_YES : DbConsts.ENABLED_NO)
                        .eq(User::getId, req.getId()));
        if (count != 1) throw new BusinessException("更新用户信息失败");
    }

    /**
     * 列出所有用户
     * @return 用户响应列表
     */
    public List<UserResp> listAllUsers() {
        // 筛选部门管理查看的数据
        List<Long> manageUserIds = null;
        if (!SecurityUtils.isSuperAdmin()) {
            List<Long> manageNodeIds = SecurityUtils.getManageNodeIds();
            if (manageNodeIds.isEmpty()) return List.of();
            List<Member> members = memberMapper.selectList(
                    Wrappers.<Member>lambdaQuery().in(Member::getNodeId, manageNodeIds));
            manageUserIds = members.stream().map(Member::getNodeId).toList();
        }

        // 查询用户
        List<User> users = userMapper.selectList(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getDeleted, DbConsts.DELETED_NO)
                        .in(manageUserIds != null && !manageUserIds.isEmpty(), User::getId, manageUserIds));
        if (users == null || users.isEmpty()) return List.of();
        List<Long> validUserIds = users.stream().map(User::getId).toList();

        // 查询用户配额
        List<Drive> drives = driveMapper.selectList(
                Wrappers.<Drive>lambdaQuery()
                        .eq(Drive::getDriveType, DbConsts.DRIVE_TYPE_PERSONAL)
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
        List<UserResp> resp = users.stream()
                .map(user -> UserResp.builder()
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

        return resp;
    }

    /**
     * 为用户分配系统角色
     * @param req 角色分配请求
     */
    @Transactional
    public void assignSystemRole(SystemRoleAssignmentReq req) {
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
                .filter(id -> id != null && id > 0)
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
            if (insertCount != addUserRoles.size()) throw new BusinessException("分配系统角色失败");
        }

        // 删除角色关联
        if (!deleteRoleIds.isEmpty()) {
            int deleteCount = userRoleMapper.delete(
                    Wrappers.<UserRole>lambdaQuery()
                            .eq(UserRole::getUserId, userId)
                            .in(UserRole::getRoleId, deleteRoleIds));
            if (deleteCount != deleteRoleIds.size()) throw new BusinessException("分配系统角色失败");
        }
    }

    /**
     * 重置用户密码为默认密码
     * @param req 密码重置请求
     */
    @Transactional
    public void resetPassword(PasswordResetReq req) {
        // 查询用户
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getId, req.getUserId())
                        .eq(User::getDeleted, DbConsts.DELETED_NO)
                        .eq(User::getEnabled, DbConsts.ENABLED_YES));
        if (user == null) throw new BusinessException("用户不存在");

        // 重置密码
        String defaultPassword = configService.getDefaultPassword();
        int count = userMapper.update(
                Wrappers.<User>lambdaUpdate()
                        .set(User::getPassword, passwordEncoder.encode(defaultPassword))
                        .eq(User::getId, req.getUserId()));
        if (count != 1) throw new BusinessException("用户删除失败");
    }

    /**
     * 列出所有系统角色
     * @return 系统角色列表
     */
    public List<Role> listSystemRole() {
        List<Role> systemRoles = roleMapper.selectList(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getType, DbConsts.ROLE_TYPE_SYSTEM)
                        .eq(Role::getEnabled, DbConsts.ENABLED_YES)
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        return systemRoles;
    }
}