package org.example.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("file_storage")
public class Storage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 物理文件记录ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 文件原始上传名称 */
    @TableField("original_name")
    private String originalName;

    /** 文件后缀 */
    @TableField("file_ext")
    private String fileExt;

    /** 文件大小(字节) */
    @TableField("file_size")
    private Long fileSize;

    /** 文件唯一标识 */
    @TableField("sha256")
    private String sha256;

    /** MinIO Bucket */
    @TableField("bucket_name")
    private String bucketName;

    /** MinIO ObjectKey */
    @TableField("object_key")
    private String objectKey;

    /** 文件MIME类型 */
    @TableField("mime_type")
    private String mimeType;

    /** 引用计数(指向该物理文件的逻辑文件数量) */
    @TableField("ref_count")
    private Integer refCount;

    /** 首次上传时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 首次创建者ID */
    @TableField("creator_id")
    private Long creatorId;
}
