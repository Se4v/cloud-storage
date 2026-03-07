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
@TableName("sys_log")
public class Log implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 日志记录ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 用户名称 */
    @TableField("username")
    private String username;

    /** 请求路径 */
    @TableField("request_uri")
    private String requestUri;

    /** 请求参数(JSON) */
    @TableField("request_params")
    private String requestParams;

    /** 操作: UPLOAD/DOWNLOAD/LOGIN/DELETE */
    @TableField("action_type")
    private String actionType;

    /** 对象类型: FILE/USER/SHARE/SYSTEM */
    @TableField("target_type")
    private String targetType;

    /** 对象ID */
    @TableField("target_id")
    private Long targetId;

    /** 对象名称 */
    @TableField("target_name")
    private String targetName;

    /** 操作详情 */
    @TableField("content")
    private String content;

    /** 是否成功:0-失败; 1-成功 */
    @TableField("is_success")
    private Integer success;

    /** 错误信息 */
    @TableField("error_msg")
    private String errorMsg;

    /** IP地址 */
    @TableField("client_ip")
    private String clientIp;

    /** 设备信息 */
    @TableField("device_info")
    private String deviceInfo;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
