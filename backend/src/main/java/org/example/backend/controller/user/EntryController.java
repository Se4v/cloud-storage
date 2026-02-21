package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/entry")
public class EntryController {

    @PostMapping("")
    public Result<?> createFolder() {
        return null;
    }

    @PostMapping("")
    public Result<?> getEntries() {
        return null;
    }

    @PostMapping("")
    public Result<?> moveEntry() {
        return null;
    }

    @PostMapping("")
    public Result<?> renameEntry() {
        return null;
    }

    @PostMapping("")
    public Result<?> searchEntry() {
        return null;
    }

    @PostMapping("")
    public Result<?> deleteEntry() {
        return null;
    }
}
