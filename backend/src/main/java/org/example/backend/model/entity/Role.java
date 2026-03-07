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
@TableName("sys_role")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色名称 */
    @TableField("name")
    private String name;

    /** 角色代码 */
    @TableField("code")
    private String code;

    /** 角色类型:1-全局角色; 2-组织内角色 */
    @TableField("type")
    private Integer type;

    /** 是否启用:0-禁用; 1-启用 */
    @TableField("is_enabled")
    private Integer enabled;

    /** 是否删除:0-未删除; 1-已删除 */
    @TableField("is_deleted")
    private Integer deleted;

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
