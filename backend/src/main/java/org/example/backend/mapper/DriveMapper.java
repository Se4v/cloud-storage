package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Drive;

import java.util.List;
import java.util.Map;

public interface DriveMapper extends BaseMapper<Drive> {
    /**
     * 获取总配额和已使用配额的和
     * @return Map 包含 "sumTotalQuota" 和 "sumUsedQuota"
     */
    Map<String, Object> selectQuotaSums();

    /**
     * 统计个人与企业空间的配额
     */
    List<Map<String, Object>> selectQuotaSumByType();
}
