package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Storage;

public interface StorageMapper extends BaseMapper<Storage> {
    Storage selectBySha256(String sha256);

    Integer increaseRefCountBySha256(String sha256);
}
