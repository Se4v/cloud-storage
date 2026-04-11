package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.request.AssignGlobalRoleArgs;
import org.example.backend.model.request.CreateUserArgs;
import org.example.backend.model.request.DeleteUserArgs;
import org.example.backend.model.request.UpdateUserArgs;
import org.example.backend.model.entity.Role;
import org.example.backend.model.response.RoleView;
import org.example.backend.model.response.UserView;
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

    @PostMapping("/create")
    public Result<?> createUser(@RequestBody CreateUserArgs args) {
        userService.createUser(args);
        return Result.success("创建用户成功");
    }

    @PostMapping("/delete")
    public Result<?> deleteUsers(@RequestBody DeleteUserArgs args) {
        userService.deleteUsers(args);
        return Result.success("删除用户成功");
    }

    @PostMapping("/update")
    public Result<?> updateUser(@RequestBody UpdateUserArgs args) {
        userService.updateUser(args);
        return Result.success("更新用户成功");
    }

    @GetMapping("/all")
    public Result<?> listAllUsers() {
        List<UserView> views = userService.listAllUsers();
        return Result.success("", views);
    }

    @PostMapping("/assign")
    public Result<?> assignGlobalRole(@RequestBody AssignGlobalRoleArgs args) {
        userService.assignGlobalRole(args);
        return Result.success("<UNK>");
    }

    @PostMapping("/reset")
    public Result<?> resetPassword(Long id) {
        userService.resetPassword(id);
        return Result.success("<UNK>");
    }

    @GetMapping("/role")
    public Result<?> listGlobalRole() {
        List<Role> roleList = userService.listGlobalRole();
        List<RoleView> views = roleList.stream()
                .map(role -> RoleView.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build()).toList();

        return Result.success("", views);
    }
}
