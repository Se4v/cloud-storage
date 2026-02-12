package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("org_node")
public class Node implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("node_name")
    private String nodeName;

    @TableField("node_type")
    private Integer nodeType;

    @TableField("parent_id")
    private Long parentId;

    @TableField("is_enabled")
    private Boolean enabled;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("updater_id")
    private Long updaterId;
}
