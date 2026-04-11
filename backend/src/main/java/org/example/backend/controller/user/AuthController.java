package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.LoginArgs;
import org.example.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginArgs args) {
        String token = authService.login(args.getUsername(), args.getPassword());
        return Result.success("", token);
    }

    @GetMapping("/logout")
    public Result<?> logout() {
        String token = SecurityUtil.getToken();
        authService.logout(token);
        return Result.success();
    }
}
