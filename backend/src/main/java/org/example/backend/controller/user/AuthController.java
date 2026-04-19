package org.example.backend.controller.user;

import org.example.backend.common.annotation.OperationLog;
import org.example.backend.common.result.Result;
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

    /**
     * 用户登录
     * @param req 登录请求参数
     * @return 认证令牌Token
     */
    @OperationLog(module = "认证模块", action = "LOGIN", targetType = "USER")
    @PostMapping("/login")
    public Result<?> login(@RequestBody AuthReq req) {
        String token = authService.login(req);
        return Result.success(token);
    }

    /**
     * 用户登出
     * @return 统一响应结果
     */
    @OperationLog(module = "认证模块", action = "LOGOUT", targetType = "USER")
    @GetMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }
}
