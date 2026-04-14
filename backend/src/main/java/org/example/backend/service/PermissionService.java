package org.example.backend.service;

import org.example.backend.mapper.PermissionMapper;
import org.example.backend.model.entity.Permission;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * 获取权限数据列表
     * @return 权限实体列表
     */
    public List<Permission> listAllPermissions() {
        return permissionMapper.selectList(null);
    }
}
