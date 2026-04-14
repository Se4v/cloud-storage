package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("org_node")
public class Node implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 节点名称 */
    @TableField("node_name")
    private String nodeName;

    /** 节点类型:1-公司; 2-部门; 3-团队 */
    @TableField("node_type")
    private Integer nodeType;

    /** 上级节点ID(根节点为0) */
    @TableField("parent_id")
    private Long parentId;

    /** 是否启用:0-禁用; 1-启用 */
    @TableField("is_enabled")
    private Integer isEnabled;

    /** 是否删除:0-未删除; 1-已删除 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 创建者ID */
    @TableField("creator_id")
    private Long creatorId;
}
