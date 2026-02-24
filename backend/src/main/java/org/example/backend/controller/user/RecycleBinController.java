package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.MyUserDetails;
import org.example.backend.model.result.RecycleDetailResult;
import org.example.backend.model.view.RecycleBinView;
import org.example.backend.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/recycle")
public class RecycleBinController {
    @Autowired
    private RecycleBinService recycleBinService;

    public Result<List<RecycleBinView>> getRecycleBinEntries() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        List<RecycleDetailResult> results = recycleBinService.getEntries(userDetails.getUserId());

        List<RecycleBinView> recycleBinViews = results.stream()
                .map(result -> {
                    return RecycleBinView.builder()
                            .id(String.valueOf(result.getEntryId()))
                            .name(result.getEntryName())
                            .drive(result.getDriveName())
                            .size(String.valueOf(result.getFileSize()))
                            .deleteTime(String.valueOf(result.getDeletedAt()))
                            .expireTime(String.valueOf(result.getExpiredAt()))
                            .build();
                })
                .toList();

        return Result.success("", recycleBinViews);
    }


    public Result<Void> restoreEntries(@RequestBody List<Long> ids) {
        recycleBinService.restoreEntries(ids);
        return Result.success();
    }

    public Result<Void> permanentlyDeleteEntries(@RequestBody List<Long> ids) {
        recycleBinService.permanentlyDeleteEntries(ids);
        return Result.success();
    }

    public Result<Void> clear() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        recycleBinService.clear(userDetails.getUserId());

        return Result.success();
    }
}
