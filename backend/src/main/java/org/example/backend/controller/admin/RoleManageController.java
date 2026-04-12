package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.perm.PermAssignmentReq;
import org.example.backend.model.request.role.RoleCreationReq;
import org.example.backend.model.request.role.RoleDeletionReq;
import org.example.backend.model.request.role.RoleUpdateReq;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.perm.PermissionView;
import org.example.backend.model.response.role.RoleView;
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
    public Result<?> createRole(@RequestBody RoleCreationReq req) {
        roleService.createRole(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteRoles(@RequestBody RoleDeletionReq req) {
        roleService.deleteRoles(req);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> updateRole(@RequestBody RoleUpdateReq req) {
        roleService.updateRole(req);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<?> listAllRoles() {
        List<Role> roleList = roleService.listAllRoles();
        List<RoleView> resp = roleList.stream()
                .map(role -> RoleView.builder()
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

    @PostMapping("/assign")
    public Result<?> assignPermissions(@RequestBody PermAssignmentReq req) {
        roleService.assignPermissions(req);
        return Result.success();
    }

    @GetMapping("/perm")
    public Result<?> listPermissions() {
        List<Permission> permissionList = roleService.listPermissions();
        List<PermissionView> resp = permissionList.stream()
                .map(permission -> PermissionView.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .build())
                .toList();
        return Result.success(resp);
    }
}
