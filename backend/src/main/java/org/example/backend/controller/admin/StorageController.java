package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.UpdateStorageArgs;
import org.example.backend.model.entity.Storage;
import org.example.backend.model.view.StorageView;
import org.example.backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    @Autowired
    private StorageService storageService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/update")
    public Result<Void> updateStorageItems(@RequestBody UpdateStorageArgs args) {
        storageService.updateStorageItems(args);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<List<StorageView>> listAllStorageItems() {
        List<Storage> storageList = storageService.listAllStorageItems();
        List<StorageView> storageViewList = storageList.stream()
                .map(storage -> StorageView.builder()
                        .id(storage.getId())
                        .name(storage.getOriginalName())
                        .uploadTime(storage.getCreatedAt().format(formatter))
                        .size(storage.getFileSize())
                        .isEnabled(storage.getEnabled() == 1)
                        .refCount(storage.getRefCount())
                        .build())
                .toList();
        return Result.success(storageViewList);
    }
}
