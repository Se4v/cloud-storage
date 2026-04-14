package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.role.SystemRoleAssignmentReq;
import org.example.backend.model.request.user.PasswordResetReq;
import org.example.backend.model.request.user.UserCreationReq;
import org.example.backend.model.request.user.UserDeletionReq;
import org.example.backend.model.request.user.UserUpdateReq;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.role.RoleResp;
import org.example.backend.model.response.user.UserResp;
import org.example.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserManageController {
    private final UserService userService;

    public UserManageController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 创建用户
     * @param req 用户创建请求参数
     * @return 统一响应结果
     */
    @PostMapping("/create")
    public Result<?> createUser(@RequestBody UserCreationReq req) {
        userService.createUser(req);
        return Result.success();
    }

    /**
     * 批量删除用户
     * @param req 用户删除请求参数
     * @return 统一响应结果
     */
    @PostMapping("/delete")
    public Result<?> deleteUsers(@RequestBody UserDeletionReq req) {
        userService.deleteUsers(req);
        return Result.success();
    }

    /**
     * 更新用户信息
     * @param req 用户更新请求参数
     * @return 统一响应结果
     */
    @PostMapping("/update")
    public Result<?> updateUser(@RequestBody UserUpdateReq req) {
        userService.updateUser(req);
        return Result.success();
    }

    /**
     * 查询所有用户列表
     * @return 用户信息列表
     */
    @GetMapping("/all")
    public Result<?> listAllUsers() {
        List<UserResp> resp = userService.listAllUsers();
        return Result.success(resp);
    }

    /**
     * 为用户分配系统角色
     * @param req 系统角色分配请求参数
     * @return 统一响应结果
     */
    @PostMapping("/assign")
    public Result<?> assignSystemRole(@RequestBody SystemRoleAssignmentReq req) {
        userService.assignSystemRole(req);
        return Result.success();
    }

    /**
     * 重置用户密码
     * @param req 密码重置请求参数
     * @return 统一响应结果
     */
    @PostMapping("/reset")
    public Result<?> resetPassword(@RequestBody PasswordResetReq req) {
        userService.resetPassword(req);
        return Result.success();
    }

    /**
     * 查询系统角色列表
     * @return 精简后的系统角色信息列表
     */
    @GetMapping("/role")
    public Result<?> listSystemRole() {
        List<Role> roleList = userService.listSystemRole();
        List<RoleResp> resp = roleList.stream()
                .map(role -> RoleResp.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build()).toList();

        return Result.success(resp);
    }
}
