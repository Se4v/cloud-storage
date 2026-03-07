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
@TableName("org_member")
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 成员记录ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 组织节点ID(部门/团队) */
    @TableField("node_id")
    private Long nodeId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 在该部门内的角色ID(如:部门经理/普通员工) */
    @TableField("role_id")
    private Long roleId;

    /** 是否为主管/负责人:0-否; 1-是 */
    @TableField("is_leader")
    private Integer leader;

    /** 是否删除:0-未删除; 1-已删除 */
    @TableField("is_deleted")
    private Integer deleted;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 创建者ID */
    @TableField("creator_id")
    private Long creatorId;
}
