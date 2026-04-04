package org.example.backend.common.util;

import org.example.backend.common.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    // 获取当前登录的完整用户信息
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        throw new RuntimeException("用户未登录");
    }

    // 快捷获取当前用户的 userId
    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getToken() {
        return getLoginUser().getToken();
    }
}
