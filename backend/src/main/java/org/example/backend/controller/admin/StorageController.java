package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    @PostMapping("/")
    public Result<Void> updateStorageItem() {
        return null;
    }

    @GetMapping("/all")
    public Result<Void> listAllStorageItems() {
        return null;
    }
}
