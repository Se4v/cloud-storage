package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.view.PermissionView;
import org.example.backend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/perm")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    public Result<List<PermissionView>> listALlPermissions() {
        List<Permission> results = permissionService.getAllPermissions();

        List<PermissionView> permissionViews = results.stream()
                .map(result -> PermissionView.builder()
                        .id(result.getId())
                        .name(result.getName())
                        .code(result.getCode())
                        .build())
                .toList();

        return Result.success("", permissionViews);
    }
}
