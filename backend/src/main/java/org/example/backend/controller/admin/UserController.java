package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    public Result<Void> createUser() {
        return null;
    }

    public Result<Void> deleteUsers() {
        return null;
    }

    public Result<Void> updateUser() {
        return null;
    }

    public Result<Void> listAllUsers() {
        return null;
    }

    public Result<Void> assignGlobalRole() {
        return null;
    }

    public Result<Void> resetPassword() {
        return null;
    }

    public Result<Void> forceLogout() {
        return null;
    }
}
