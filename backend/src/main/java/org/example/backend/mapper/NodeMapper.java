package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.model.entity.Node;
import org.example.backend.model.result.NodeDetailResult;

import java.util.List;

public interface NodeMapper extends BaseMapper<Node> {

    @Select("SELECT " +
            "  n.id, " +
            "  n.node_name AS name, " +
            "  n.node_type AS type, " +
            "  n.parent_id AS parentId, " +
            "  p.node_name AS parentName " +
            "FROM org_node n " +
            "LEFT JOIN org_node p ON n.parent_id = p.id " +
            "WHERE n.enabled = 1 " +
            "ORDER BY n.parent_id ASC, n.id ASC")
    List<NodeDetailResult> selectNode();
}
