package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.result.RecycleDetailResult;
import org.example.backend.model.view.RecycleView;
import org.example.backend.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recycle")
public class RecycleBinController {
    @Autowired
    private RecycleBinService recycleBinService;

    public Result<List<RecycleView>> listEntries() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        List<RecycleDetailResult> results = recycleBinService.listEntries(userDetails.getUserId());

        List<RecycleView> recycleViews = results.stream()
                .map(result -> {
                    return RecycleView.builder()
                            .id(String.valueOf(result.getEntryId()))
                            .name(result.getEntryName())
                            .drive(result.getDriveName())
                            .size(String.valueOf(result.getFileSize()))
                            .deleteTime(String.valueOf(result.getDeletedAt()))
                            .expireTime(String.valueOf(result.getExpiredAt()))
                            .build();
                })
                .toList();

        return Result.success("", recycleViews);
    }


    public Result<Void> restoreEntries(@RequestBody List<Long> ids) {
        recycleBinService.restoreEntries(ids);
        return Result.success();
    }

    public Result<Void> deleteEntries(@RequestBody List<Long> ids) {
        recycleBinService.permanentlyDeleteEntries(ids);
        return Result.success();
    }

    public Result<Void> clearEntries() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        recycleBinService.clear(userDetails.getUserId());

        return Result.success();
    }
}
