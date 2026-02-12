package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class Log implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_params")
    private String requestParams;

    @TableField("action_type")
    private String actionType;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_name")
    private String targetName;

    @TableField("content")
    private String content;

    @TableField("is_success")
    private Boolean success;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("client_ip")
    private String clientIp;

    @TableField("device_info")
    private String deviceInfo;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
