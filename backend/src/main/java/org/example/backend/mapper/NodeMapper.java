package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Node;

public interface NodeMapper extends BaseMapper<Node> {
    List<Node> select
}
