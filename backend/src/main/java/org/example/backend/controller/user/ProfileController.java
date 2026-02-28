package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.ChangePasswordArgs;
import org.example.backend.model.args.UpdateProfileArgs;
import org.example.backend.model.args.UploadAvatarArgs;
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

    @GetMapping()
    public Result<ProfileView> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        ProfileView view = profileService.getProfile(userDetails.getUserId());

        return Result.success("", view);
    }

    @GetMapping("/avatar")
    public Result<?> getAvatar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        String avatarUrl = profileService.getAvatar(userDetails.getUserId());

        return Result.success("", avatarUrl);
    }

    @PostMapping()
    public Result<Void> updateProfile(@RequestBody UpdateProfileArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        profileService.updateProfile(args, userDetails.getUserId());

        return Result.success("");
    }

    @PostMapping("/upload")
    public Result<Void> uploadAvatar(@RequestBody UploadAvatarArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        profileService.uploadAvatar(args, userDetails.getUserId());

        return Result.success("");
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        profileService.changePassword(args, userDetails.getUserId());

        return Result.success("");
    }

}
