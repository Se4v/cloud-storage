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
@TableName("file_share")
public class Share implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 分享记录ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 空间ID */
    @TableField("drive_id")
    private Long driveId;

    /** 条目ID */
    @TableField("entry_id")
    private Long entryId;

    /** 条目类型:1-文件; 2-文件夹 */
    @TableField("entry_type")
    private Integer entryType;

    /** 分享者ID */
    @TableField("user_id")
    private Long userId;

    /** 分享链接名称 */
    @TableField("link_name")
    private String linkName;

    /** 分享链接唯一标识 */
    @TableField("link_key")
    private String linkKey;

    /** 分享类型:1-公开链接; 2-加密链接 */
    @TableField("link_type")
    private Integer linkType;

    /** 加密分享提取码 */
    @TableField("access_code")
    private String accessCode;

    /** 是否删除:0-未删除; 1-已删除 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 过期时间 */
    @TableField("expired_at")
    private LocalDateTime expiredAt;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
