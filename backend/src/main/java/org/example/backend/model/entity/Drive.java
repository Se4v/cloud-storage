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

    /** 空间ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 空间名称 */
    @TableField("drive_name")
    private String driveName;

    /** 空间类型:1-个人; 2-企业 */
    @TableField("drive_type")
    private Integer driveType;

    /** 归属组织节点ID(仅空间类型=公司/部门/团队时有效,个人空间为0) */
    @TableField("node_id")
    private Long nodeId;

    /** 关联用户ID(个人空间=所属用户ID;公司/部门/团队空间=管理员用户ID) */
    @TableField("user_id")
    private Long userId;

    /** 总存储配额(字节) */
    @TableField("total_quota")
    private Long totalQuota;

    /** 已使用存储配额(字节) */
    @TableField("used_quota")
    private Long usedQuota;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 创建者ID */
    @TableField("creator_id")
    private Long creatorId;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 更新者ID */
    @TableField("updater_id")
    private Long updaterId;
}
