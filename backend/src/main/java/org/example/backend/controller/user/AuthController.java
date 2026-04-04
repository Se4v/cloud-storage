package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.LoginUser;
import org.example.backend.model.args.LoginArgs;
import org.example.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginArgs args) {
        String token = authService.login(args.getUsername(), args.getPassword());

        return Result.success("", token);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) auth.getPrincipal();

        authService.logout(loginUser.getToken());
        return Result.success();
    }
}
