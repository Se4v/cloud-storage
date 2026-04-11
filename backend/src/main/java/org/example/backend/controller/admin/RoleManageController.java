package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.request.AssignPermissionArgs;
import org.example.backend.model.request.CreateRoleArgs;
import org.example.backend.model.request.UpdateRoleArgs;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.PermissionView;
import org.example.backend.model.response.RoleView;
import org.example.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleManageController {
    @Autowired
    private RoleService roleService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/create")
    public Result<Void> createRole(@RequestBody CreateRoleArgs args) {
        roleService.createRole(args);
        return Result.success("");
    }

    @PostMapping("/delete")
    public Result<Void> deleteRoles(@RequestBody List<Long> roleIds) {
        roleService.deleteRoles(roleIds);
        return Result.success("");
    }

    @PostMapping("/update")
    public Result<Void> updateRole(@RequestBody UpdateRoleArgs args) {
        roleService.updateRole(args);
        return Result.success("");
    }

    @GetMapping("/all")
    public Result<List<RoleView>> listAllRoles() {
        List<Role> roleList = roleService.listAllRoles();
        List<RoleView> roleViews = roleList.stream()
                .map(role -> RoleView.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .code(role.getCode())
                        .type(role.getType() == 1 ? "global" : "org")
                        .createTime(role.getCreatedAt().format(formatter))
                        .isEnabled(role.getEnabled() == 1)
                        .build())
                .toList();
        return Result.success(roleViews);
    }

    @PostMapping("/assign")
    public Result<Void> assignPermissions(@RequestBody AssignPermissionArgs args) {
        roleService.assignPermissions(args);
        return Result.success("");
    }

    @GetMapping("/perm")
    public Result<List<PermissionView>> listPermissions() {
        List<Permission> permissionList = roleService.listPermissions();
        List<PermissionView> permissionViews = permissionList.stream()
                .map(permission -> PermissionView.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .build())
                .toList();
        return Result.success("", permissionViews);
    }
}
