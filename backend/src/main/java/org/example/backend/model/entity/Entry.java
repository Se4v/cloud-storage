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

    /** 条目ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 空间ID */
    @TableField("drive_id")
    private Long driveId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 父目录ID(根目录为0,仅文件类型为文件夹时有效) */
    @TableField("parent_id")
    private Long parentId;

    /** 关联的物理文件ID(文件类型为文件夹时为0) */
    @TableField("storage_id")
    private Long storageId;

    /** 文件/目录名称 */
    @TableField("entry_name")
    private String entryName;

    /** 条目类型:1-文件; 2-文件夹 */
    @TableField("entry_type")
    private Integer entryType;

    /** 文件大小(字节) */
    @TableField("file_size")
    private Long fileSize;

    /** 文件后缀 */
    @TableField("file_ext")
    private String fileExt;

    /** 状态:1-未删除; 2-已删除; 3-永久删除; */
    @TableField("status")
    private Integer status;

    /** 删除时间 */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /** 删除后的过期时间 */
    @TableField("expired_at")
    private LocalDateTime expiredAt;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 更新者ID */
    @TableField("updater_id")
    private Long updaterId;
}
