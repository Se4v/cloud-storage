package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Node;

import java.util.List;

public interface NodeMapper extends BaseMapper<Node> {
    List<Node> selectNodeWithParents(List<Long> id);
}
