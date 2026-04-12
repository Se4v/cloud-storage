package org.example.backend.service;

import org.example.backend.mapper.LogMapper;
import org.example.backend.model.entity.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {
    private final LogMapper logMapper;

    public LogService(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    public List<Log> listAllLogs() {
        return logMapper.selectList(null);
    }

    @Async
    public void saveLogAsync(Log log) {
        try {
            logMapper.insert(log);
        } catch (Exception e) {
            // 异步任务中的异常需要单独处理，防止吞掉错误
            logger.error("异步保存日志失败", e);
        }
    }
}
