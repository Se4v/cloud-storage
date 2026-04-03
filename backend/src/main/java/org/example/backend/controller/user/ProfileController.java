package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.model.args.ChangePasswordArgs;
import org.example.backend.model.args.UpdateAvatarArgs;
import org.example.backend.model.args.UpdateProfileArgs;
import org.example.backend.model.entity.User;
import org.example.backend.model.view.AvatarUploadUrlView;
import org.example.backend.model.view.ProfileView;
import org.example.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    Long userId = 2034965772877197313L;

    /**
     * 获取头像链接
     * @return 头像URL字符串
     */
    @GetMapping("/avatar")
    public Result<String> getAvatar() {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        // String avatarUrl = profileService.getAvatar(userDetails.getUserId());
        String avatarUrl = profileService.getAvatar(userId);

        return Result.success("", avatarUrl);
    }

    /**
     * 获取头像直传链接
     * @param fileName 前端准备上传的文件名 (例如 "avatar.png")
     * @return 包含直传链接、策略和 objectName 的 Map
     */
    @GetMapping("/avatar/upload-url")
    public Result<AvatarUploadUrlView> getAvatarUploadUrl(@RequestParam String fileName,
                                                          @RequestParam(required = false) Long fileSize) {
        AvatarUploadUrlView view = profileService.getAvatarUploadUrl(fileName, fileSize, userId);
        return Result.success("", view);
    }

    /**
     * 更新数据库中的头像信息
     * @param args 头像信息参数
     * @return 空响应
     */
    @PostMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody UpdateAvatarArgs args) {
        profileService.updateAvatar(args, userId);
        return Result.success("");
    }

    /**
     * 获取个人信息
     * @return 个人信息视图对象
     */
    @GetMapping
    public Result<ProfileView> getProfile() {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        User user = profileService.getProfile(userId);
        // User user = profileService.getProfile(userDetails.getUserId());
        ProfileView view = ProfileView.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .build();

        return Result.success("", view);
    }

    /**
     * 更新个人信息
     * @param args 个人信息更新参数（不含密码、头像）
     * @return 空响应
     */
    @PostMapping
    public Result<Void> updateProfile(@RequestBody UpdateProfileArgs args) {
        profileService.updateProfile(args);
        return Result.success("");
    }

    /**
     * 修改密码
     * @param args 密码修改参数（原密码、新密码）
     * @return 空响应
     */
    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody ChangePasswordArgs args) {
        profileService.updatePassword(args);
        return Result.success("");
    }
}
