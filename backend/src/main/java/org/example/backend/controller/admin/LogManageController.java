package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.response.log.LogResp;
import org.example.backend.service.LogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                        .operationObject(log.getTargetType())
                        .operationTime(log.getCreatedAt())
                        .success(log.getStatus())
                        .build())
                .toList();
        return Result.success(resp);
    }

    /**
     * 获取单条日志记录
     * @param id 日志ID
     * @return 详细日志记录
     */
    @GetMapping("/detail")
    public Result<?> getLogDetail(@RequestParam Long id) {
        String logJson = logService.getLogDetail(id);
        return Result.success(logJson);
    }

    /**
     * 导出日志
     * @return 文件流响应实体
     */
    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> export() {
        String fileName = "log-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH")) + ".csv";
        StreamingResponseBody stream = logService.export();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        return ResponseEntity.ok().headers(headers).body(stream);
    }
}
