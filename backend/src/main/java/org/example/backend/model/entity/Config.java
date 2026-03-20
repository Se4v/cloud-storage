package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置表实体类
 */
@Data
@Builder
@TableName("sys_config")
public class Config implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 配置键 */
    @TableField("config_key")
    private String configKey;

    /** 配置值(统一存为字符串) */
    @TableField("config_value")
    private String configValue;

    /** 配置描述 */
    @TableField("description")
    private String description;

    /** 是否启用: 1-启用; 0-禁用 */
    @TableField("is_enabled")
    private Integer enabled;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
