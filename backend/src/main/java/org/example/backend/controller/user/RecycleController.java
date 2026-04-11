package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.DeleteEntryArgs;
import org.example.backend.model.request.RestoreEntryArgs;
import org.example.backend.model.response.RecycleView;
import org.example.backend.service.RecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycle")
public class RecycleController {
    @Autowired
    private RecycleService recycleService;

    @GetMapping
    public Result<?> listEntries() {
        Long currentUserId = SecurityUtil.getUserId();
        List<RecycleView> recycleViewList = recycleService.listEntries(currentUserId);
        return Result.success("", recycleViewList);
    }


    @PostMapping("/restore")
    public Result<?> restoreEntries(@RequestBody RestoreEntryArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        recycleService.restoreEntries(args, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody DeleteEntryArgs args) {
        Long currentUserId = SecurityUtil.getUserId();
        recycleService.deleteEntries(args, currentUserId);
        return Result.success();
    }

    @PostMapping("/clear")
    public Result<?> clearEntries() {
        Long currentUserId = SecurityUtil.getUserId();
        recycleService.clearEntries(currentUserId);
        return Result.success();
    }
}
