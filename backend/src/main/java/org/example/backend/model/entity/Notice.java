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
@TableName("sys_notice")
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("type")
    private Integer type;

    @TableField("target_id")
    private Long targetId;

    @TableField("is_read")
    private Integer read;

    @TableField("is_deleted")
    private Integer deleted;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
