package org.example.backend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.example.backend.aspect.LogContextHolder;
import org.example.backend.common.constant.DbConsts;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.*;
import org.example.backend.model.request.perm.PermAssignmentReq;
import org.example.backend.model.request.role.RoleCreationReq;
import org.example.backend.model.request.role.RoleDeletionReq;
import org.example.backend.model.request.role.RoleUpdateReq;
import org.example.backend.model.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleMapper roleMapper;
    private final MemberMapper memberMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public RoleService(RoleMapper roleMapper, MemberMapper memberMapper, UserRoleMapper userRoleMapper,
                       PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.memberMapper = memberMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 创建角色
     * @param req 角色创建请求
     */
    @Transactional
    public void createRole(RoleCreationReq req) {
        // 校验角色编码是否重复
        long sameCode = roleMapper.selectCount(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getCode, req.getCode())
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        if (sameCode > 0) throw new BusinessException("角色编码已存在");

        // 校验角色名称是否重复
        long sameName = roleMapper.selectCount(
                Wrappers.<Role>lambdaQuery()
                        .eq(Role::getName, req.getName())
                        .eq(Role::getDeleted, DbConsts.DELETED_NO));
        if (sameName > 0) throw new BusinessException("角色名称已存在");

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("创建角色");
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("code", req.getCode());
        logMap.put("name", req.getName());
        logMap.put("type", req.getType());
        LogContextHolder.addDetailProperty("role_create", logMap);

        // 创建角色
        Role role = Role.builder()
                .name(req.getName())
                .code(req.getCode())
                .type(req.getType())
                .build();
        int count = roleMapper.insert(role);
        if (count != 1) throw new BusinessException("创建角色失败");
    }

    /**
     * 批量删除角色
     * @param req 角色删除请求
     */
    @Transactional
    public void deleteRoles(RoleDeletionReq req) {
        // 查询组织角色是否被使用
        long usedMember = memberMapper.selectCount(
                Wrappers.<Member>lambdaQuery()
                        .eq(Member::getDeleted, DbConsts.DELETED_NO)
                        .in(Member::getRoleId, req.getRoleIds()));
        if (usedMember > 0) throw new BusinessException("部分组织角色仍被成员使用，无法删除");

        // 查询全局角色是否被使用
        long usedSystemRole = userRoleMapper.selectCount(
                Wrappers.<UserRole>lambdaQuery().in(UserRole::getRoleId, req.getRoleIds()));
        if (usedSystemRole > 0) throw new BusinessException("部分全局角色仍被用户绑定，无法删除");

        // 删除角色
        int roleDeleteCount = roleMapper.update(
                Wrappers.<Role>lambdaUpdate()
                        .set(Role::getDeleted, DbConsts.DELETED_YES)
                        .in(Role::getId, req.getRoleIds()));
        if (roleDeleteCount == 0) throw new BusinessException("删除角色失败");

        LogContextHolder.setTargetId(0L);
        LogContextHolder.setTargetName("批量删除" + req.getRoleIds().size() + "个角色");
        List<Role> roles = roleMapper.selectList(Wrappers.<Role>lambdaQuery().in(Role::getId, req.getRoleIds()));
        List<Map<String,Object>> logs = roles.stream()
                .map(role -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", role.getId());
                    map.put("code", role.getCode());
                    map.put("name", role.getName());
                    map.put("type", role.getType());
                    return map;
                })
                .toList();
        LogContextHolder.addDetailProperty("role_delete", logs);

        // 删除组织角色关联
        memberMapper.update(
                Wrappers.<Member>lambdaUpdate()
                        .set(Member::getDeleted, DbConsts.DELETED_YES)
                        .in(Member::getRoleId, req.getRoleIds()));

        // 删除全局角色关联
        userRoleMapper.delete(
                Wrappers.<UserRole>lambdaQuery().in(UserRole::getRoleId, req.getRoleIds()));

        // 删除角色-权限关联
        rolePermissionMapper.delete(
                Wrappers.<RolePermission>lambdaQuery().in(RolePermission::getRoleId, req.getRoleIds()));
    }

    /**
     * 更新角色
     * @param req 角色更新请求
     */
    @Transactional
    public void updateRole(RoleUpdateReq req) {
        // 校验角色是否存在
        Role existingRole = roleMapper.selectById(req.getId());
        if (existingRole == null) throw new BusinessException("角色不存在");

        // 校验角色编码是否重复
        if (req.getCode() != null && !req.getCode().equals(existingRole.getCode())) {
            long sameCode = roleMapper.selectCount(
                    Wrappers.<Role>lambdaQuery()
                            .eq(Role::getCode, req.getCode())
                            .ne(Role::getId, req.getId()));
            if (sameCode > 0) throw new BusinessException("角色编码已存在");
        }

        // 校验角色名称是否重复
        if (req.getName() != null && !req.getName().equals(existingRole.getName())) {
            long sameName = roleMapper.selectCount(
                    Wrappers.<Role>lambdaQuery()
                            .eq(Role::getName, req.getName())
                            .ne(Role::getId, req.getId()));
            if (sameName > 0) throw new BusinessException("角色名称已存在");
        }

        LogContextHolder.setTargetId(req.getId());
        LogContextHolder.setTargetName("更新角色信息");
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("oldCode", existingRole.getCode());
        logMap.put("newCode", req.getCode());
        logMap.put("oldName", existingRole.getName());
        logMap.put("newName", req.getName());
        logMap.put("enabled", req.getIsEnabled());
        LogContextHolder.addDetailProperty("role_update", logMap);

        // 更新角色
        int count = roleMapper.update(
                Wrappers.<Role>lambdaUpdate()
                        .set(Role::getName, req.getName())
                        .set(Role::getCode, req.getCode())
                        .set(Role::getEnabled, req.getIsEnabled())
                        .eq(Role::getId, req.getId()));
        if (count != 1) throw new BusinessException("更新角色失败");
    }

    /**
     * 查询所有角色
     * @return 角色列表
     */
    public List<Role> listAllRoles() {
        List<Role> allRoles = roleMapper.selectList(
                Wrappers.<Role>lambdaQuery().eq(Role::getDeleted, DbConsts.DELETED_NO));
        return allRoles;
    }

    /**
     * 分配权限
     * @param req 权限分配请求
     */
    @Transactional
    public void assignPermissions(PermAssignmentReq req) {
        // 基础参数校验
        Long roleId = req.getRoleId();
        // 处理permissionIds：null视为清空所有权限，转为空列表
        List<Long> targetPermissionIds = req.getPermissionIds() == null ? List.of() : req.getPermissionIds();

        // 查询该角色当前已绑定的权限
        List<RolePermission> boundedPermissions = rolePermissionMapper.selectList(
                Wrappers.<RolePermission>lambdaQuery().eq(RolePermission::getRoleId, roleId));

        // 提取已有的权限ID集合
        Set<Long> existingPermissionIds = boundedPermissions.stream()
                .map(RolePermission::getPermId)
                .collect(Collectors.toSet());
        // 目标权限ID集合
        Set<Long> targetPermissionIdSet = targetPermissionIds.stream()
                .filter(id -> id != null && id > 0) // 过滤无效ID
                .collect(Collectors.toSet());

        // 计算需要新增的权限
        Set<Long> addPermissionIds = targetPermissionIdSet.stream()
                .filter(id -> !existingPermissionIds.contains(id))
                .collect(Collectors.toSet());
        // 计算需要删除的权限
        Set<Long> deletePermissionIds = existingPermissionIds.stream()
                .filter(id -> !targetPermissionIdSet.contains(id))
                .collect(Collectors.toSet());

        LogContextHolder.setTargetId(req.getRoleId());
        LogContextHolder.setTargetName("角色分配权限");
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("roleId", roleId);
        logMap.put("originPermIds", existingPermissionIds);
        logMap.put("newPermIds", targetPermissionIds);
        LogContextHolder.addDetailProperty("role_assign", logMap);

        // 新增权限关联
        if (!addPermissionIds.isEmpty()) {
            List<RolePermission> addRolePermissions = addPermissionIds.stream()
                    .map(permissionId -> RolePermission.builder()
                                .roleId(roleId)
                                .permId(permissionId)
                                .build())
                    .toList();
            List<BatchResult> results = rolePermissionMapper.insert(addRolePermissions);
            int insertCount = results.stream()
                    .flatMapToInt(result -> Arrays.stream(result.getUpdateCounts()))
                    .sum();
            if (insertCount != addRolePermissions.size()) throw new BusinessException("分配权限失败");
        }

        // 删除权限关联
        if (!deletePermissionIds.isEmpty()) {
            int deleteCount = rolePermissionMapper.delete(
                    Wrappers.<RolePermission>lambdaQuery()
                            .eq(RolePermission::getRoleId, roleId)
                            .in(RolePermission::getPermId, deletePermissionIds));
            if (deleteCount != deletePermissionIds.size()) throw new BusinessException("分配权限失败");
        }
    }

    /**
     * 列出所有权限
     * @return 权限列表
     */
    public List<Permission> listPermissions() {
        List<Permission> permissions = permissionMapper.selectList(
                Wrappers.<Permission>lambdaQuery().eq(Permission::getType, 2));
        return permissions;
    }
}
