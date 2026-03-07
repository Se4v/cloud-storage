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
@TableName("sys_role_perm")
public class RolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 角色-权限关联ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色ID */
    @TableField("role_id")
    private Long roleId;

    /** 权限ID */
    @TableField("perm_id")
    private Long permId;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 创建者ID */
    @TableField("creator_id")
    private Long creatorId;
}
