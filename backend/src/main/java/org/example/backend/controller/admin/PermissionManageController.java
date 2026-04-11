package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.response.PermissionView;
import org.example.backend.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/perm")
public class PermissionManageController {
    private final PermissionService permissionService;

    public PermissionManageController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/all")
    public Result<?> listALlPermissions() {
        List<Permission> permissionList = permissionService.listAllPermissions();

        List<PermissionView> views = permissionList.stream()
                .map(permission -> {
                    return PermissionView.builder()
                            .id(permission.getId())
                            .name(permission.getName())
                            .code(permission.getCode())
                            .type(permission.getType())
                            .build();
                })
                .toList();

        return Result.success("", views);
    }
}
