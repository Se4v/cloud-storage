package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.MemberMapper;
import org.example.backend.mapper.UserRoleMapper;
import org.example.backend.mapper.RoleMapper;
import org.example.backend.mapper.RolePermissionMapper;
import org.example.backend.model.args.CreateRoleArgs;
import org.example.backend.model.args.UpdateRoleArgs;
import org.example.backend.model.entity.Member;
import org.example.backend.model.entity.Role;
import org.example.backend.model.entity.UserRole;
import org.example.backend.model.entity.RolePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    private static final int ENABLED = 1;
    private static final int DELETED = 1;

    /**
     * 创建角色
     */
    @Transactional
    public void createRole(CreateRoleArgs args, Long userId) {
        // 校验角色编码是否重复
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getCode, args.getCode());
        if (roleMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        // 校验角色名称是否重复
        LambdaQueryWrapper<Role> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(Role::getName, args.getName());
        if (roleMapper.selectCount(nameQuery) > 0) {
            throw new BusinessException("角色名称已存在");
        }

        // 创建角色
        Role role = Role.builder()
                .name(args.getName())
                .code(args.getCode())
                .type(args.getType())
                .enabled(ENABLED)
                .createdAt(LocalDateTime.now())
                .creatorId(userId)
                .build();

        int count = roleMapper.insert(role);
        if (count != 1) {
            throw new BusinessException("创建角色失败");
        }
    }

    /**
     * 批量删除角色
     */
    @Transactional
    public void deleteRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        // 删除角色
        LambdaUpdateWrapper<Role> roleUpdate = new LambdaUpdateWrapper<>();
        roleUpdate.set(Role::getDeleted, DELETED)
                .in(Role::getId, roleIds);
        if (roleMapper.update(roleUpdate) != roleIds.size()) {
            throw new BusinessException("删除角色失败");
        }

        // 级联删除组织-用户关联
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.in(Member::getRoleId, roleIds);
        long memberCount = memberMapper.selectCount(memberQuery);
        if (memberCount > 0) {
            throw new BusinessException("该角色还在被使用，无法删除");
        }
        LambdaUpdateWrapper<Member> memberUpdate = new LambdaUpdateWrapper<>();
        memberUpdate.set(Member::getDeleted, DELETED)
                .in(Member::getRoleId, roleIds);
        if (memberMapper.update(memberUpdate) != roleIds.size()) {
            throw new BusinessException("<UNK>");
        }

        // 级联删除用户-角色关联
        LambdaQueryWrapper<UserRole> assignmentQuery = new LambdaQueryWrapper<>();
        assignmentQuery.in(UserRole::getRoleId, roleIds);
        long assignmentCount = userRoleMapper.selectCount(assignmentQuery);
        if (assignmentCount > 0) {
            throw new BusinessException("该角色还在被使用，无法删除");
        }
        if (userRoleMapper.delete(assignmentQuery) != roleIds.size()) {
            throw new BusinessException("<UNK>");
        }

        // 级联删除角色-权限关联
        LambdaQueryWrapper<RolePermission> permissionQuery =
                new LambdaQueryWrapper<>();
        permissionQuery.in(RolePermission::getRoleId, roleIds);
        if (rolePermissionMapper.delete(permissionQuery) != roleIds.size()) {
            throw new BusinessException("<UNK>");
        }
    }

    /**
     * 更新角色
     */
    @Transactional
    public void updateRole(UpdateRoleArgs args, Long userId) {
        if (args.getId() == null) {
            throw new BusinessException("角色ID不能为空");
        }

        // 校验角色是否存在
        Role existingRole = roleMapper.selectById(args.getId());
        if (existingRole == null) {
            throw new BusinessException("角色不存在");
        }

        // 校验角色编码是否重复（如果编码有变更）
        if (args.getCode() != null && !args.getCode().equals(existingRole.getCode())) {
            LambdaQueryWrapper<Role> codeQuery = new LambdaQueryWrapper<>();
            codeQuery.eq(Role::getCode, args.getCode())
                    .ne(Role::getId, args.getId());
            if (roleMapper.selectCount(codeQuery) > 0) {
                throw new BusinessException("角色编码已存在");
            }
        }

        // 校验角色名称是否重复（如果名称有变更）
        if (args.getName() != null && !args.getName().equals(existingRole.getName())) {
            LambdaQueryWrapper<Role> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Role::getName, args.getName())
                    .ne(Role::getId, args.getId());
            if (roleMapper.selectCount(nameQuery) > 0) {
                throw new BusinessException("角色名称已存在");
            }
        }

        // 构建更新条件
        LambdaUpdateWrapper<Role> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(args.getName() != null, Role::getName, args.getName())
                .set(args.getCode() != null, Role::getCode, args.getCode())
                .set(args.getType() != null, Role::getType, args.getType())
                .set(args.getEnabled() != null, Role::getEnabled, args.getEnabled())
                .eq(Role::getId, args.getId());

        int count = roleMapper.update(updateWrapper);
        if (count != 1) {
            throw new BusinessException("更新角色失败");
        }
    }

    /**
     * 查询所有角色
     */
    public List<Role> listAllRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Role::getId);
        return roleMapper.selectList(queryWrapper);
    }
}
