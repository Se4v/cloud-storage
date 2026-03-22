package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.view.RecycleView;
import org.example.backend.service.RecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycle")
public class RecycleController {
    @Autowired
    private RecycleService recycleService;

    Long userId = 2034965772877197313L;

    @GetMapping
    public Result<List<RecycleView>> listEntries() {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // List<RecycleView> recycleViewList = recycleService.listEntries(userDetails.getUserId());

        List<RecycleView> recycleViewList = recycleService.listRecycleEntries(userId);

        return Result.success("", recycleViewList);
    }


    @PostMapping("/restore")
    public Result<Void> restoreEntries(@RequestBody List<Long> ids) {
        recycleService.restoreRecycleEntries(ids);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteEntries(@RequestBody List<Long> ids) {
        recycleService.permanentlyDeleteRecycleEntries(ids);
        return Result.success();
    }

    @PostMapping("/clear")
    public Result<Void> clearEntries() {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();
        // recycleService.clearRecycleEntries(userDetails.getUserId());

        return Result.success();
    }
}
