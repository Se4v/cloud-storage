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

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("drive_id")
    private Long driveId;

    @TableField("entry_id")
    private Long entryId;

    @TableField("entry_type")
    private Integer entryType;

    @TableField("user_id")
    private Long userId;

    @TableField("link_name")
    private String linkName;

    @TableField("link_key")
    private String linkKey;

    @TableField("link_type")
    private Integer linkType;

    @TableField("access_code")
    private String accessCode;

    @TableField("is_deleted")
    private Integer deleted;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
