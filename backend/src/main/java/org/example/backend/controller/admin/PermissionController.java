package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perm")
public class PermissionController {
    public Result<Void> listPermissions() {
        return null;
    }
}
