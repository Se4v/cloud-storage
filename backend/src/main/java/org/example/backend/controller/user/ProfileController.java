package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.ChangePasswordArgs;
import org.example.backend.model.args.UpdateProfileArgs;
import org.example.backend.model.args.UploadAvatarArgs;
import org.example.backend.model.entity.User;
import org.example.backend.model.view.ProfileView;
import org.example.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    /**
     * 获取个人信息
     * @return 个人信息视图对象
     */
    @GetMapping
    public Result<ProfileView> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        User user = profileService.getProfile(userDetails.getUserId());
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
     * 获取头像链接
     * @return 头像URL字符串
     */
    @GetMapping("/avatar")
    public Result<String> getAvatar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        String avatarUrl = profileService.getAvatar(userDetails.getUserId());

        return Result.success("", avatarUrl);
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
     * 上传头像
     * @param args 头像上传参数（包含头像文件）
     * @return 空响应
     */
    @PostMapping("/avatar")
    public Result<Void> uploadAvatar(@ModelAttribute UploadAvatarArgs args) {
        profileService.uploadAvatar(args);

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
