package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.model.entity.Storage;

import java.util.Map;

public interface StorageMapper extends BaseMapper<Storage> {
    Storage selectBySha256(String sha256);

    Integer increaseRefCountBySha256(String sha256);

    int batchUpdateRefCount(@Param("countMap") Map<Long, Integer> countMap);
}
