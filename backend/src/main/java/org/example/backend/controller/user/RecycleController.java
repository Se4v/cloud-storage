package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.file.DeleteEntryArgs;
import org.example.backend.model.request.file.RestoreEntryArgs;
import org.example.backend.model.response.RecycleView;
import org.example.backend.service.RecycleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycle")
public class RecycleController {
    private final RecycleService recycleService;

    public RecycleController(RecycleService recycleService) {
        this.recycleService = recycleService;
    }

    @GetMapping
    public Result<?> listEntries() {
        Long currentUserId = SecurityUtil.getUserId();
        List<RecycleView> resp = recycleService.listEntries(currentUserId);
        return Result.success(resp);
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
