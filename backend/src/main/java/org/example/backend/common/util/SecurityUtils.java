package org.example.backend.common.util;

import org.example.backend.common.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class SecurityUtils {
    /**
     * 获取当前登录的完整用户信息
     * @return 登录用户实体对象
     * @throws RuntimeException 用户未登录时抛出异常
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 获取当前登录用户真实姓名
     * @return 真实姓名
     */
    public static String getRealName() {
        return getLoginUser().getRealName();
    }

    /**
     * 获取当前登录用户Token
     * @return 认证令牌
     */
    public static String getToken() {
        return getLoginUser().getToken();
    }

    /**
     * 获取当前登录用户所属组织节点ID
     * @return 组织节点ID
     */
    public static Long getOrgId() {
        return getLoginUser().getOrgId();
    }

    /**
     * 判断当前用户是否为超级管理员
     * @return true-是超级管理员，false-不是
     */
    public static boolean isSuperAdmin() {
        return getLoginUser().isSuperAdmin();
    }

    /**
     * 获取当前用户管辖的节点ID列表
     * @return 管辖节点ID集合
     */
    public static List<Long> getManageNodeIds() {
        return getLoginUser().getManageNodeIds();
    }
}
