package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateRoleArgs;
import org.example.backend.model.args.UpdateRoleArgs;
import org.example.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping("/create")
    public Result<Void> createRole(@RequestBody CreateRoleArgs args) {
        return null;
    }

    @PostMapping("/delete")
    public Result<Void> deleteRoles(@RequestBody List<Long> roleIds) {
        return null;
    }

    @PostMapping("/update")
    public Result<Void> updateRole(@RequestBody UpdateRoleArgs args) {
        return null;
    }

    @GetMapping("/all")
    public Result<Void> listAllRoles() {
        return null;
    }

    @PostMapping("")
    public Result<Void> assignPermissions() {
        return null;
    }
}
