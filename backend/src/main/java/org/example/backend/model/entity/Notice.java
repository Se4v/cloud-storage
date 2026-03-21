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

    /** 通知记录ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 通知标题 */
    @TableField("title")
    private String title;

    /** 通知内容 */
    @TableField("content")
    private String content;

    /** 通知类型: 1-系统公告; 2-系统告警 */
    @TableField("type")
    private Integer type;

    /** 接收者ID,为0则是全体用户 */
    @TableField("target_id")
    private Long targetId;

    /** 是否已读: 0-未读; 1-已读 */
    @TableField("is_read")
    private Integer isRead;

    /** 是否删除: 0-未删除; 1-已删除 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 过期时间 */
    @TableField("expired_at")
    private LocalDateTime expiredAt;

    /** 通知时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
