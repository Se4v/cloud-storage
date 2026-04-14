package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.entity.Permission;
import org.example.backend.model.response.perm.PermResp;
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

    /**
     * 获取所有权限列表
     * @return 权限列表数据
     */
    @GetMapping("/all")
    public Result<?> listALlPermissions() {
        List<Permission> permissionList = permissionService.listAllPermissions();

        List<PermResp> resp = permissionList.stream()
                .map(permission -> {
                    return PermResp.builder()
                            .id(permission.getId())
                            .name(permission.getName())
                            .code(permission.getCode())
                            .type(permission.getType())
                            .build();
                })
                .toList();

        return Result.success(resp);
    }
}
