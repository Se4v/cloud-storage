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

    @GetMapping("/avatar")
    public Result<String> getAvatar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        String avatarUrl = profileService.getAvatar(userDetails.getUserId());

        return Result.success("", avatarUrl);
    }

    @PostMapping
    public Result<Void> updateProfile(@RequestBody UpdateProfileArgs args) {
        profileService.updateProfile(args);

        return Result.success("");
    }

    @PostMapping("/avatar")
    public Result<Void> uploadAvatar(@ModelAttribute UploadAvatarArgs args) {
        profileService.uploadAvatar(args);

        return Result.success("");
    }

    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody ChangePasswordArgs args) {
        profileService.updatePassword(args);

        return Result.success("");
    }

}
