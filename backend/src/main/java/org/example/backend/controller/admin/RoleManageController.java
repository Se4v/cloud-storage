package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.request.AssignPermissionArgs;
import org.example.backend.model.request.CreateRoleArgs;
import org.example.backend.model.request.DeleteRoleReq;
import org.example.backend.model.request.UpdateRoleArgs;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.PermissionView;
import org.example.backend.model.response.RoleView;
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

    @PostMapping("/create")
    public Result<?> createRole(@RequestBody CreateRoleArgs args) {
        roleService.createRole(args);
        return Result.success("");
    }

    @PostMapping("/delete")
    public Result<?> deleteRoles(@RequestBody DeleteRoleReq req) {
        roleService.deleteRoles(req);
        return Result.success("");
    }

    @PostMapping("/update")
    public Result<?> updateRole(@RequestBody UpdateRoleArgs args) {
        roleService.updateRole(args);
        return Result.success("");
    }

    @GetMapping("/all")
    public Result<?> listAllRoles() {
        List<Role> roleList = roleService.listAllRoles();
        List<RoleView> views = roleList.stream()
                .map(role -> RoleView.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .code(role.getCode())
                        .type(role.getType())
                        .createTime(role.getCreatedAt())
                        .isEnabled(role.getEnabled())
                        .build())
                .toList();
        return Result.success("", views);
    }

    @PostMapping("/assign")
    public Result<?> assignPermissions(@RequestBody AssignPermissionArgs args) {
        roleService.assignPermissions(args);
        return Result.success("");
    }

    @GetMapping("/perm")
    public Result<?> listPermissions() {
        List<Permission> permissionList = roleService.listPermissions();
        List<PermissionView> views = permissionList.stream()
                .map(permission -> PermissionView.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .build())
                .toList();
        return Result.success("", views);
    }
}
