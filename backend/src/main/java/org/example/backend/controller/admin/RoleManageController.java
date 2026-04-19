package org.example.backend.controller.admin;

import org.example.backend.common.annotation.OperationLog;
import org.example.backend.common.result.Result;
import org.example.backend.model.request.perm.PermAssignmentReq;
import org.example.backend.model.request.role.RoleCreationReq;
import org.example.backend.model.request.role.RoleDeletionReq;
import org.example.backend.model.request.role.RoleUpdateReq;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.perm.PermResp;
import org.example.backend.model.response.role.RoleResp;
import org.example.backend.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleManageController {
    private final RoleService roleService;

    public RoleManageController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 创建角色
     * @param req 角色创建请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "角色管理模块", action = "CREATE", targetType = "ORLE")
    @PostMapping("/create")
    public Result<?> createRole(@RequestBody RoleCreationReq req) {
        roleService.createRole(req);
        return Result.success();
    }

    /**
     * 批量删除角色
     * @param req 角色删除请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "角色管理模块", action = "DELETE", targetType = "ORLE")
    @PostMapping("/delete")
    public Result<?> deleteRoles(@RequestBody RoleDeletionReq req) {
        roleService.deleteRoles(req);
        return Result.success();
    }

    /**
     * 更新角色信息
     * @param req 角色更新请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "角色管理模块", action = "UPDATE", targetType = "ORLE")
    @PostMapping("/update")
    public Result<?> updateRole(@RequestBody RoleUpdateReq req) {
        roleService.updateRole(req);
        return Result.success();
    }

    /**
     * 查询所有角色列表
     * @return 封装后的角色响应列表
     */
    @GetMapping("/all")
    public Result<?> listAllRoles() {
        List<Role> roleList = roleService.listAllRoles();
        List<RoleResp> resp = roleList.stream()
                .map(role -> RoleResp.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .code(role.getCode())
                        .type(role.getType())
                        .createTime(role.getCreatedAt())
                        .isEnabled(role.getEnabled())
                        .build())
                .toList();
        return Result.success(resp);
    }

    /**
     * 为角色分配权限
     * @param req 权限分配请求参数
     * @return 统一响应结果
     */
    @OperationLog(module = "角色管理模块", action = "ASSIGN", targetType = "ORLE")
    @PostMapping("/assign")
    public Result<?> assignPermissions(@RequestBody PermAssignmentReq req) {
        roleService.assignPermissions(req);
        return Result.success();
    }

    /**
     * 查询系统所有权限列表
     * @return 精简后的权限响应列表
     */
    @GetMapping("/perm")
    public Result<?> listPermissions() {
        List<Permission> permissionList = roleService.listPermissions();
        List<PermResp> resp = permissionList.stream()
                .map(permission -> PermResp.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .build())
                .toList();
        return Result.success(resp);
    }
}
