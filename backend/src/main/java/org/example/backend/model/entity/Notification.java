package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("type")
    private Integer type;

    @TableField("is_read")
    private Boolean read;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
