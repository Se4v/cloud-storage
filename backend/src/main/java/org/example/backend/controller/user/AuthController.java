package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.MyUserDetails;
import org.example.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<String> login(String username, String password) {
        String token = authService.login(username, password);

        return Result.success("", token);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal MyUserDetails userDetails) {
        authService.logout(userDetails.getToken());

        return Result.success();
    }
}
