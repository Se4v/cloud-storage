package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class PersonalDriveController {
    @GetMapping()
    public Result<?> listEntries(Long parentId, Long userId) {


    }

    @PostMapping
    public Result<?> createEntry() {

    }

    @PostMapping
    public Result<?> moveEntries() {

    }

    @PostMapping
    public Result<?> renameEntry() {

    }

    @PostMapping
    public Result<?> searchEntry() {

    }

    @PostMapping
    public Result<?> deleteEntries() {

    }
}
