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
@TableName("sys_perm")
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 权限ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 权限名称 */
    @TableField("name")
    private String name;

    /** 权限代码 */
    @TableField("code")
    private String code;

    /** 权限类型:1-菜单权限; 2-操作权限; 3-数据权限 */
    @TableField("type")
    private Integer type;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
