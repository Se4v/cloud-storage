package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_log")
public class Log implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志记录ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 组织节点ID */
    @TableField("org_node_id")
    private Long orgNodeId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 用户名称 */
    @TableField("username")
    private String username;

    /** 真实姓名 */
    @TableField("real_name")
    private String realName;

    /** 功能模块 */
    @TableField("module")
    private String module;

    /** 操作: UPLOAD/DOWNLOAD/LOGIN/DELETE */
    @TableField("action")
    private String action;

    /** 对象类型: FILE/USER/SHARE/SYSTEM */
    @TableField("target_type")
    private String targetType;

    /** 对象ID */
    @TableField("target_id")
    private Long targetId;

    /** 对象名称 */
    @TableField("target_name")
    private String targetName;

    /** 请求路径 */
    @TableField("request_uri")
    private String requestUri;

    /** 操作详情 */
    @TableField("detail")
    private String detail;

    /** 是否成功:0-失败; 1-成功 */
    @TableField("status")
    private Integer status;

    /** 错误信息 */
    @TableField("error_msg")
    private String errorMsg;

    /** IP地址 */
    @TableField("client_ip")
    private String clientIp;

    /** 设备信息 */
    @TableField("cost_time")
    private Long costTime;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
