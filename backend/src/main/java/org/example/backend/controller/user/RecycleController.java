package org.example.backend.controller.user;

import org.example.backend.common.result.Result;
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
        List<RecycleResp> resp = recycleService.listEntries();
        return Result.success(resp);
    }

    @PostMapping("/restore")
    public Result<?> restoreEntries(@RequestBody EntryRestoreReq req) {
        recycleService.restoreEntries(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody EntryDeletionReq req) {
        recycleService.deleteEntries(req);
        return Result.success();
    }

    @PostMapping("/clear")
    public Result<?> clearEntries() {
        recycleService.clearEntries();
        return Result.success();
    }
}
