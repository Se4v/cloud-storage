package org.example.backend.service;

import org.example.backend.mapper.LogMapper;
import org.example.backend.model.entity.Log;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {
    private final LogMapper logMapper;

    public LogService(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    public List<Log> listAllLogs() {
        return logMapper.selectList(null);
    }
}
