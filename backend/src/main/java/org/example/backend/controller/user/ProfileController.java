package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.user.PasswordChangeReq;
import org.example.backend.model.request.user.AvatarUpdateReq;
import org.example.backend.model.request.user.ProfileUpdateReq;
import org.example.backend.model.entity.User;
import org.example.backend.model.response.user.AvatarUploadResp;
import org.example.backend.model.response.user.ProfileResp;
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
        String avatarUrl = profileService.getAvatar();
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
        AvatarUploadResp resp = profileService.getAvatarUploadUrl(fileName, fileSize);
        return Result.success(resp);
    }

    /**
     * 更新数据库中的头像信息
     * @param req 头像信息参数
     * @return 空响应
     */
    @PostMapping("/avatar")
    public Result<?> updateAvatar(@RequestBody AvatarUpdateReq req) {
        profileService.updateAvatar(req);
        return Result.success();
    }

    /**
     * 获取个人信息
     * @return 个人信息视图对象
     */
    @GetMapping
    public Result<?> getProfile() {
        User user = profileService.getProfile();

        ProfileResp resp = ProfileResp.builder()
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
        profileService.updateProfile(req);
        return Result.success();
    }

    /**
     * 修改密码
     * @param req 密码修改参数（原密码、新密码）
     * @return 空响应
     */
    @PostMapping("/password")
    public Result<?> updatePassword(@RequestBody PasswordChangeReq req) {
        profileService.updatePassword(req);
        return Result.success();
    }
}
