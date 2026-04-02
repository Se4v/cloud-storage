package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Traffic;

import java.util.List;
import java.util.Map;

public interface TrafficMapper extends BaseMapper<Traffic> {
    List<Map<String, Object>> selectTodayTrafficSumByType();

    List<Map<String, Object>> selectLast7DaysTraffic();
}
