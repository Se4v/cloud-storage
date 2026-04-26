package org.example.backend.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.mapper.LogMapper;
import org.example.backend.model.entity.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

    /**
     * 获取操作日志详细
     * @param logId 操作日志ID
     * @return 操作日志详细
     */
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

    public StreamingResponseBody export() {
        return outputStream -> {
            List<Log> logs = logMapper.selectList(null);
            EasyExcel.write(outputStream, Log.class)
                    .excelType(ExcelTypeEnum.CSV) // 指定为 CSV
                    .sheet("日志表")              // 指定 sheet 名称
                    .doWrite(logs);
        };
    }
}
