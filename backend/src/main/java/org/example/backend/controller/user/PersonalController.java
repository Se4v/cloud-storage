package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
    @GetMapping()
    public Result<?> listEntries(Long parentId, Long userId) {
        return Result.success();
    }

    @PostMapping
    public Result<?> createEntry() {
        return Result.success();
    }

    @PostMapping
    public Result<?> moveEntries() {
        return Result.success();
    }

    @PostMapping
    public Result<?> renameEntry() {
        return Result.success();
    }

    @PostMapping
    public Result<?> searchEntry() {
        return Result.success();
    }

    @PostMapping
    public Result<?> deleteEntries() {
        return Result.success();
    }
}
