package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("sys_drive")
public class Drive implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("drive_name")
    private String driveName;

    @TableField("drive_type")
    private Integer driveType;

    @TableField("node_id")
    private Long nodeId;

    @TableField("user_id")
    private Long userId;

    @TableField("total_quota")
    private Long totalQuota;

    @TableField("used_quota")
    private Long usedQuota;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("updater_id")
    private Long updaterId;
}
