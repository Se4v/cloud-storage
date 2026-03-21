package org.example.backend.service;

import org.example.backend.mapper.PermissionMapper;
import org.example.backend.model.entity.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    @Autowired
    private PermissionMapper permissionMapper;

    public List<Permission> listAllPermissions() {
        return permissionMapper.selectList(null);
    }
}
