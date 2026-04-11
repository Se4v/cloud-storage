package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.executor.BatchResult;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.*;
import org.example.backend.model.request.AssignPermissionArgs;
import org.example.backend.model.request.CreateRoleArgs;
import org.example.backend.model.request.UpdateRoleArgs;
import org.example.backend.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    private static final int ENABLED = 1;
    private static final int DELETED = 1;

    /**
     * 创建角色
     */
    @Transactional
    public void createRole(CreateRoleArgs args) {
        if (args == null) throw new BusinessException("<UNK>");

        // 校验角色编码是否重复
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getCode, args.getCode()).eq(Role::getDeleted, 0);
        if (roleMapper.selectCount(queryWrapper) > 0) throw new BusinessException("角色编码已存在");

        // 校验角色名称是否重复
        LambdaQueryWrapper<Role> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(Role::getName, args.getName()).eq(Role::getDeleted, 0);
        if (roleMapper.selectCount(nameQuery) > 0) throw new BusinessException("角色名称已存在");

        // 创建角色
        Role role = Role.builder()
                .name(args.getName())
                .code(args.getCode())
                .type(args.getType().equals("global") ? 1 : 2)
                .build();
        int roleInsertCount = roleMapper.insert(role);
        if (roleInsertCount != 1) throw new BusinessException("创建角色失败");
    }

    /**
     * 批量删除角色
     */
    @Transactional
    public void deleteRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) throw new BusinessException("<UNK>");

        // 查询组织角色是否被使用
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getDeleted, 0).in(Member::getRoleId, roleIds);
        long memberCount = memberMapper.selectCount(memberQuery);
        if (memberCount > 0) throw new BusinessException("部分组织角色仍被成员使用，无法删除");

        // 查询全局角色是否被使用
        LambdaQueryWrapper<UserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.in(UserRole::getRoleId, roleIds);
        long userRoleCount = userRoleMapper.selectCount(userRoleQuery);
        if (userRoleCount > 0) throw new BusinessException("部分全局角色仍被用户绑定，无法删除");

        // 删除角色
        LambdaUpdateWrapper<Role> roleUpdate = new LambdaUpdateWrapper<>();
        roleUpdate.set(Role::getDeleted, DELETED).in(Role::getId, roleIds);
        int roleDeleteCount = roleMapper.update(roleUpdate);
        if (roleDeleteCount == 0) throw new BusinessException("删除角色失败");

        // 删除组织角色关联
        LambdaUpdateWrapper<Member> memberUpdate = new LambdaUpdateWrapper<>();
        memberUpdate.set(Member::getDeleted, DELETED).in(Member::getRoleId, roleIds);
        memberMapper.update(memberUpdate);

        // 删除全局角色关联
        LambdaQueryWrapper<UserRole> userRoleDeleteQuery = new LambdaQueryWrapper<>();
        userRoleDeleteQuery.in(UserRole::getRoleId, roleIds);
        userRoleMapper.delete(userRoleDeleteQuery);

        // 删除角色-权限关联
        LambdaQueryWrapper<RolePermission> permissionQuery = new LambdaQueryWrapper<>();
        permissionQuery.in(RolePermission::getRoleId, roleIds);
        rolePermissionMapper.delete(permissionQuery);
    }

    /**
     * 更新角色
     */
    @Transactional
    public void updateRole(UpdateRoleArgs args) {
        if (args == null) throw new BusinessException("<UNK>");

        // 校验角色是否存在
        Role existingRole = roleMapper.selectById(args.getId());
        if (existingRole == null) throw new BusinessException("角色不存在");

        // 校验角色编码是否重复
        if (args.getCode() != null && !args.getCode().equals(existingRole.getCode())) {
            LambdaQueryWrapper<Role> codeQuery = new LambdaQueryWrapper<>();
            codeQuery.eq(Role::getCode, args.getCode())
                    .ne(Role::getId, args.getId());
            if (roleMapper.selectCount(codeQuery) > 0) throw new BusinessException("角色编码已存在");
        }

        // 校验角色名称是否重复
        if (args.getName() != null && !args.getName().equals(existingRole.getName())) {
            LambdaQueryWrapper<Role> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Role::getName, args.getName())
                    .ne(Role::getId, args.getId());
            if (roleMapper.selectCount(nameQuery) > 0) throw new BusinessException("角色名称已存在");
        }

        // 更新角色
        LambdaUpdateWrapper<Role> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getName() != null, Role::getName, args.getName())
                .set(args.getCode() != null, Role::getCode, args.getCode())
                .set(args.getIsEnabled() != null, Role::getEnabled, Boolean.TRUE.equals(args.getIsEnabled()) ? 1 : 0)
                .eq(Role::getId, args.getId());

        int count = roleMapper.update(updateWrapper);
        if (count != 1) throw new BusinessException("更新角色失败");
    }

    /**
     * 查询所有角色
     */
    public List<Role> listAllRoles() {
        LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(Role::getDeleted, 0);
        return roleMapper.selectList(roleQuery);
    }

    @Transactional
    public void assignPermissions(AssignPermissionArgs args) {
        // 基础参数校验
        if (args == null) throw new BusinessException("权限分配参数不能为空");
        Long roleId = args.getRoleId();
        // 处理permissionIds：null视为清空所有权限，转为空列表
        List<Long> targetPermissionIds = args.getPermissionIds() == null ? List.of() : args.getPermissionIds();

        // 查询该角色当前已绑定的权限
        LambdaQueryWrapper<RolePermission> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> existingRolePermissions = rolePermissionMapper.selectList(roleQuery);

        // 提取已有的权限ID集合
        Set<Long> existingPermissionIds = existingRolePermissions.stream()
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
            if (insertCount != addRolePermissions.size()) throw new BusinessException("<UNK>");
        }

        // 删除权限关联
        if (!deletePermissionIds.isEmpty()) {
            LambdaQueryWrapper<RolePermission> deleteQuery = new LambdaQueryWrapper<>();
            deleteQuery.eq(RolePermission::getRoleId, roleId).in(RolePermission::getPermId, deletePermissionIds);
            int deleteCount = rolePermissionMapper.delete(deleteQuery);
            if (deleteCount != deletePermissionIds.size()) throw new BusinessException("<UNK>");
        }
    }

    public List<Permission> listPermissions() {
        LambdaQueryWrapper<Permission> permissionQuery = new LambdaQueryWrapper<>();
        permissionQuery.eq(Permission::getType, 2);
        return permissionMapper.selectList(permissionQuery);
    }
}
