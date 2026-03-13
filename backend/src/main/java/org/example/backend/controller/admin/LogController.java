package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log")
public class LogController {
    @Autowired
    private LogService logService;

    public Result<?> listLogs() {
        return null;
    }
}
