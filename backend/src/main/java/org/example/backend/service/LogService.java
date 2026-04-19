package org.example.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    public LogService(LogMapper logMapper, ObjectMapper objectMapper) {
        this.logMapper = logMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 列出所有操作日志
     * @return 操作日志列表
     */
    public List<Log> listAllLogs() {
        return logMapper.selectList(null);
    }

    public String getLogDetail(Long logId) {
        Log log = logMapper.selectById(logId);
        String json;
        try {
             json = objectMapper.writeValueAsString(log);
        } catch (Exception e) {
            logger.error("获取详细日志失败");
            return null;
        }
        return json;
    }

    /**
     * 异步保存操作日志
     * @param log 操作日志实体
     */
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
