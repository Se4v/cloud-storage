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

    @GetMapping("/avatar")
    public Result<?> getAvatar() {
        String avatarUrl = profileService.getAvatar();
        return Result.success(avatarUrl);
    }

    @GetMapping("/avatar/upload-url")
    public Result<?> getAvatarUploadUrl(@RequestParam String fileName,
                                        @RequestParam(required = false) Long fileSize) {
        AvatarUploadResp resp = profileService.getAvatarUploadUrl(fileName, fileSize);
        return Result.success(resp);
    }

    @PostMapping("/avatar")
    public Result<?> updateAvatar(@RequestBody AvatarUpdateReq req) {
        profileService.updateAvatar(req);
        return Result.success();
    }

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

    @PostMapping
    public Result<?> updateProfile(@RequestBody ProfileUpdateReq req) {
        profileService.updateProfile(req);
        return Result.success();
    }

    @PostMapping("/password")
    public Result<?> updatePassword(@RequestBody PasswordChangeReq req) {
        profileService.updatePassword(req);
        return Result.success();
    }
}
