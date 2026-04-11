package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.response.PermissionView;
import org.example.backend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/perm")
public class PermissionManageController {
    @Autowired
    private PermissionService permissionService;

    @GetMapping("/all")
    public Result<List<PermissionView>> listALlPermissions() {
        List<Permission> permissionList = permissionService.listAllPermissions();

        List<PermissionView> permissionViews = permissionList.stream()
                .map(permission -> {
                    String type = switch (permission.getType()) {
                        case 1 -> "menu";
                        case 2 -> "operation";
                        case 3 -> "data";
                        default -> "unknown";
                    };
                    return PermissionView.builder()
                            .id(permission.getId())
                            .name(permission.getName())
                            .code(permission.getCode())
                            .type(type)
                            .build();
                })
                .toList();

        return Result.success("", permissionViews);
    }
}
