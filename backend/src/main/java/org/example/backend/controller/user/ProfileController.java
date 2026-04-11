package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.user.PasswordChangeReq;
import org.example.backend.model.request.user.AvatarUpdateReq;
import org.example.backend.model.request.user.ProfileUpdateReq;
import org.example.backend.model.entity.User;
import org.example.backend.model.response.AvatarUploadUrlView;
import org.example.backend.model.response.ProfileView;
import org.example.backend.service.ProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 获取头像链接
     * @return 头像URL字符串
     */
    @GetMapping("/avatar")
    public Result<?> getAvatar() {
        Long currentUserId = SecurityUtil.getUserId();
        String avatarUrl = profileService.getAvatar(currentUserId);
        return Result.success(avatarUrl);
    }

    /**
     * 获取头像直传链接
     * @param fileName 前端准备上传的文件名 (例如 "avatar.png")
     * @return 包含直传链接、策略和 objectName 的 Map
     */
    @GetMapping("/avatar/upload-url")
    public Result<?> getAvatarUploadUrl(@RequestParam String fileName,
                                        @RequestParam(required = false) Long fileSize) {
        Long currentUserId = SecurityUtil.getUserId();
        AvatarUploadUrlView resp = profileService.getAvatarUploadUrl(fileName, fileSize, currentUserId);
        return Result.success(resp);
    }

    /**
     * 更新数据库中的头像信息
     * @param req 头像信息参数
     * @return 空响应
     */
    @PostMapping("/avatar")
    public Result<?> updateAvatar(@RequestBody AvatarUpdateReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        profileService.updateAvatar(req, currentUserId);
        return Result.success();
    }

    /**
     * 获取个人信息
     * @return 个人信息视图对象
     */
    @GetMapping
    public Result<?> getProfile() {
        Long currentUserId = SecurityUtil.getUserId();
        User user = profileService.getProfile(currentUserId);

        ProfileView resp = ProfileView.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .build();

        return Result.success(resp);
    }

    /**
     * 更新个人信息
     * @param req 个人信息更新参数（不含密码、头像）
     * @return 空响应
     */
    @PostMapping
    public Result<?> updateProfile(@RequestBody ProfileUpdateReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        profileService.updateProfile(req, currentUserId);
        return Result.success();
    }

    /**
     * 修改密码
     * @param req 密码修改参数（原密码、新密码）
     * @return 空响应
     */
    @PostMapping("/password")
    public Result<?> updatePassword(@RequestBody PasswordChangeReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        profileService.updatePassword(req, currentUserId);
        return Result.success();
    }
}
