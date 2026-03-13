package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result<Void> createUser() {
        return null;
    }

    @PostMapping("/delete")
    public Result<Void> deleteUsers() {
        return null;
    }

    @PostMapping("/update")
    public Result<Void> updateUser() {
        return null;
    }

    @GetMapping("/all")
    public Result<Void> listAllUsers() {
        return null;
    }

    @PostMapping("")
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
