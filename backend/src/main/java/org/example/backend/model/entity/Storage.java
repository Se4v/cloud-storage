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
@TableName("file_storage")
public class Storage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("original_name")
    private String originalName;

    @TableField("file_ext")
    private String fileExt;

    @TableField("file_size")
    private Long fileSize;

    @TableField("sha256")
    private String sha256;

    @TableField("bucket_name")
    private String bucketName;

    @TableField("object_key")
    private String objectKey;

    @TableField("mime_type")
    private String mimeType;

    @TableField("is_enabled")
    private Integer enabled;

    @TableField("ref_count")
    private Integer refCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("creator_id")
    private Long creatorId;
}
