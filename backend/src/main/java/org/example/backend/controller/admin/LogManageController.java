package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.entity.Log;
import org.example.backend.model.response.LogView;
import org.example.backend.service.LogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/log")
public class LogManageController {
    private final LogService logService;

    public LogManageController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/all")
    public Result<List<LogView>> listAllLogs() {
        List<Log> logList = logService.listAllLogs();
        List<LogView> logViewList = logList.stream()
                .map(log -> LogView.builder()
                        .id(log.getId())
                        .username(log.getUsername())
                        .realName(log.getRealName())
                        .operationType(log.getAction())
                        .detail(log.getDetail())
                        .operationTime(log.getCreatedAt())
                        .success(log.getStatus() == 1)
                        .build())
                .toList();
        return Result.success(logViewList);
    }
}
