package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.auth.AuthReq;
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
    public Result<?> login(@RequestBody AuthReq req) {
        String token = authService.login(req);
        return Result.success(token);
    }

    @GetMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }
}
