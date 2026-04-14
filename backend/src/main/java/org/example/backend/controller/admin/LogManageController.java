package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.response.log.LogResp;
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

    /**
     * 获取所有操作日志列表
     * @return 操作日志列表数据
     */
    @GetMapping("/all")
    public Result<?> listAllLogs() {
        List<LogResp> resp = logService.listAllLogs().stream()
                .map(log -> LogResp.builder()
                        .id(log.getId())
                        .username(log.getUsername())
                        .realName(log.getRealName())
                        .operationType(log.getAction())
                        .detail(log.getDetail())
                        .operationTime(log.getCreatedAt())
                        .success(log.getStatus() == 1)
                        .build())
                .toList();
        return Result.success(resp);
    }
}
