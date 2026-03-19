package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateUserArgs;
import org.example.backend.model.args.DeleteUserArgs;
import org.example.backend.model.args.UpdateUserArgs;
import org.example.backend.model.entity.User;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result<Void> createUser(@RequestBody CreateUserArgs args) {
        userService.createUser(args);
        return Result.success("创建用户成功");
    }

    @PostMapping("/delete")
    public Result<Void> deleteUsers(@RequestBody DeleteUserArgs args) {
        userService.deleteUsers(args);
        return Result.success("删除用户成功");
    }

    @PostMapping("/update")
    public Result<Void> updateUser(@RequestBody UpdateUserArgs args) {
        userService.updateUser(args);
        return Result.success("更新用户成功");
    }

    @GetMapping("/all")
    public Result<List<User>> listAllUsers() {
        List<User> users = userService.listAllUsers();
        return Result.success(users);
    }

    @PostMapping("/")
    public Result<Void> assignGlobalRole() {
        return null;
    }

    @PostMapping("/reset")
    public Result<Void> resetPassword() {
        return null;
    }

}
