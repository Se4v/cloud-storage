package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.MyUserDetails;
import org.example.backend.model.args.ChangePasswordArgs;
import org.example.backend.model.args.UpdateProfileArgs;
import org.example.backend.model.args.UploadAvatarArgs;
import org.example.backend.model.view.ProfileView;
import org.example.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/profile")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @RequestMapping()
    public Result<ProfileView> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        ProfileView view = profileService.getProfile(userDetails.getUserId());

        return Result.success("", view);
    }

    @RequestMapping()
    public Result<?> getAvatar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        String avatarUrl = profileService.getAvatar(userDetails.getUserId());

        return Result.success("", avatarUrl);
    }

    @RequestMapping()
    public Result<Void> updateProfile(@RequestBody UpdateProfileArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        profileService.updateProfile(args, userDetails.getUserId());

        return Result.success("");
    }

    @RequestMapping()
    public Result<Void> uploadAvatar(@RequestBody UploadAvatarArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        profileService.uploadAvatar(args, userDetails.getUserId());

        return Result.success("");
    }

    @RequestMapping()
    public Result<Void> changePassword(@RequestBody ChangePasswordArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        profileService.changePassword(args, userDetails.getUserId());

        return Result.success("");
    }

}
