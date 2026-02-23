package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/download")
public class DownloadController {
    @Autowired
    private DownloadService downloadService;

    @PostMapping()
    public Result<?> download(@RequestBody List<Long> entryIds) {
        return null;
    }
}
