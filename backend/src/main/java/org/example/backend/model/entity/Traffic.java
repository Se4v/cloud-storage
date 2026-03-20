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
 * 文件上传下载流量记录表实体类
 */
@Data
@Builder
@TableName("sys_traffic")
public class Traffic implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 文件唯一标识/路径哈希 */
    @TableField("storage_id")
    private Long storageId;

    /** 操作类型: 1-上传, 2-下载 */
    @TableField("type")
    private Integer type;

    /** 传输文件大小(字节) */
    @TableField("file_size")
    private Long fileSize;

    /** 操作状态: 1-成功, 0-失败/中断 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
