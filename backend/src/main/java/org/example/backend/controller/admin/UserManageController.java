package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.role.SystemRoleAssignmentReq;
import org.example.backend.model.request.user.UserCreationReq;
import org.example.backend.model.request.user.UserDeletionReq;
import org.example.backend.model.request.user.UserUpdateReq;
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
    public Result<?> createUser(@RequestBody UserCreationReq req) {
        userService.createUser(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteUsers(@RequestBody UserDeletionReq req) {
        userService.deleteUsers(req);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> updateUser(@RequestBody UserUpdateReq req) {
        userService.updateUser(req);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<?> listAllUsers() {
        List<UserView> resp = userService.listAllUsers();
        return Result.success(resp);
    }

    @PostMapping("/assign")
    public Result<?> assignGlobalRole(@RequestBody SystemRoleAssignmentReq req) {
        userService.assignGlobalRole(req);
        return Result.success();
    }

    @PostMapping("/reset")
    public Result<?> resetPassword(Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @GetMapping("/role")
    public Result<?> listGlobalRole() {
        List<Role> roleList = userService.listGlobalRole();
        List<RoleView> resp = roleList.stream()
                .map(role -> RoleView.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build()).toList();

        return Result.success(resp);
    }
}
