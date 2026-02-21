package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/recycle")
public class RecycleBinController {
    public Result<?> getRecycleBinEntries() {
        return null;
    }

    public Result<?> restoreRecycleBinEntries() {
        return null;
    }

    public Result<?> permanentlyDeleteRecycleBinEntries() {
        return null;
    }

    public Result<?> clearRecycleBin() {
        return null;
    }
}
