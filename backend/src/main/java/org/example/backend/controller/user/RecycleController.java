package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
import org.example.backend.common.util.SecurityUtil;
import org.example.backend.model.request.file.EntryDeletionReq;
import org.example.backend.model.request.file.EntryRestoreReq;
import org.example.backend.model.response.file.RecycleResp;
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
        List<RecycleResp> resp = recycleService.listEntries(currentUserId);
        return Result.success(resp);
    }


    @PostMapping("/restore")
    public Result<?> restoreEntries(@RequestBody EntryRestoreReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        recycleService.restoreEntries(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody EntryDeletionReq req) {
        Long currentUserId = SecurityUtil.getUserId();
        recycleService.deleteEntries(req, currentUserId);
        return Result.success();
    }

    @PostMapping("/clear")
    public Result<?> clearEntries() {
        Long currentUserId = SecurityUtil.getUserId();
        recycleService.clearEntries(currentUserId);
        return Result.success();
    }
}
