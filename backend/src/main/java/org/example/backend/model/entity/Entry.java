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
@TableName("file_entry")
public class Entry implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("drive_id")
    private Long driveId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("blob_id")
    private Long blobId;

    @TableField("entry_name")
    private String entryName;

    @TableField("entry_type")
    private Integer entryType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_ext")
    private String fileExt;

    @TableField("is_enabled")
    private Integer enabled;

    @TableField("is_deleted")
    private Integer deleted;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("updater_id")
    private Long updaterId;
}
