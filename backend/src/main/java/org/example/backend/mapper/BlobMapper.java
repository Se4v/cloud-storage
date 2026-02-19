package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Blob;

public interface BlobMapper extends BaseMapper<Blob> {
    Blob selectBySha256(String sha256);

    Integer increaseRefCountBySha256(String sha256);
}
