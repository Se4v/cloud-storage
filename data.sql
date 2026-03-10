-- 组织节点表
CREATE TABLE `org_node` (
    `id`            bigint unsigned NOT NULL COMMENT '节点ID',
    `node_name`     varchar(64) NOT NULL DEFAULT '' COMMENT '节点名称',
    `node_type`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '节点类型:1-公司; 2-部门; 3-团队',
    `parent_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '上级节点ID(根节点为0)',
    `is_enabled`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用; 1-启用',
    `is_deleted`    tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除; 1-已删除',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    `updated_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pid_name_type` (`parent_id`, `node_name`, `node_type`),
    KEY `idx_type_enable` (`node_type`, `is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织节点表';


-- 组织成员表
CREATE TABLE `org_member` (
    `id`            bigint unsigned NOT NULL COMMENT '成员记录ID',
    `node_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '组织节点ID(部门/团队)',
    `user_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `role_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '在该部门内的角色ID(如:部门经理/普通员工)',
    `is_leader`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否为主管/负责人:0-否; 1-是',
    `is_deleted`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否删除:0-未删除; 1-已删除',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_node_user` (`node_id`, `user_id`),
    KEY `idx_user_node` (`user_id`, `node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织成员表';


-- 空间表
CREATE TABLE `sys_drive` (
    `id`             bigint unsigned NOT NULL COMMENT '空间ID',
    `drive_name`     varchar(64) NOT NULL DEFAULT '' COMMENT '空间名称',
    `drive_type`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '空间类型:1-公司; 2-部门; 3-团队; 4-个人',
    `node_id`        bigint unsigned NOT NULL DEFAULT 0 COMMENT '归属组织节点ID(仅空间类型=公司/部门/团队时有效,个人空间为0)',
    `user_id`        bigint unsigned NOT NULL DEFAULT 0 COMMENT '关联用户ID(个人空间=所属用户ID;公司/部门/团队空间=管理员用户ID)',
    `total_quota`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '总存储配额(字节)',
    `used_quota`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '已使用存储配额(字节)',
    `created_at`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    `updated_at`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bind_name` (`drive_type`, `node_id`, `user_id`, `drive_name`),
    KEY `idx_node_drive` (`node_id`, `drive_type`),
    KEY `idx_user_drive` (`user_id`, `drive_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空间表';


-- 文件存储表
CREATE TABLE `file_storage` (
    `id`                bigint unsigned NOT NULL COMMENT '物理文件记录ID',
    `original_name`     varchar(255) NOT NULL DEFAULT '' COMMENT '文件原始上传名称',
    `file_ext`          varchar(20) NOT NULL DEFAULT '' COMMENT '文件后缀',
    `file_size`         bigint unsigned NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `sha256`            char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件唯一标识',
    `bucket_name`       varchar(64) NOT NULL DEFAULT '' COMMENT 'MinIO Bucket',
    `object_key`        varchar(255) NOT NULL DEFAULT '' COMMENT 'MinIO ObjectKey',
    `mime_type`         varchar(128) NOT NULL DEFAULT '' COMMENT '文件MIME类型',
    `is_enabled`        tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用; 1-启用',
    `ref_count`         int unsigned NOT NULL DEFAULT 0 COMMENT '引用计数(指向该物理文件的逻辑文件数量)',
    `created_at`        datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次上传时间',
    `creator_id`        bigint unsigned NOT NULL DEFAULT 0 COMMENT '首次创建者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sha` (`sha256`),
    KEY `idx_ref_count` (`ref_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储表';


-- 文件条目表
CREATE TABLE `file_entry` (
    `id`            bigint unsigned NOT NULL COMMENT '条目ID',
    `drive_id`      bigint unsigned NOT NULL DEFAULT 0 COMMENT '空间ID',
    `user_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `parent_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '父目录ID(根目录为0,仅文件类型为文件夹时有效)',
    `storage_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '关联的物理文件ID(文件类型为文件夹时为0)',
    `entry_name`    varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件/目录名称',
    `entry_type`    tinyint unsigned NOT NULL DEFAULT 0 COMMENT '条目类型:1-文件; 2-文件夹',
    `file_size`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `file_ext`      varchar(32) NOT NULL DEFAULT '' COMMENT '文件后缀',
    `status`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '状态:1-未删除; 2-已删除; 3-永久删除;',
    `deleted_at`    datetime DEFAULT NULL COMMENT '删除时间',
    `deleter_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '删除者ID',
    `expired_at`    datetime DEFAULT NULL COMMENT '删除后的过期时间',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drive_pid_name` (`drive_id`, `parent_id`, `entry_name`),
    KEY `idx_recycle` (`user_id`, `status`, `deleted_at`),
    KEY `idx_drive_pid` (`drive_id`, `parent_id`),
    KEY `idx_storage_id` (`storage_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件条目表';


-- 分享表
CREATE TABLE `file_share` (
    `id`                bigint unsigned NOT NULL COMMENT '分享记录ID',
    `drive_id`          bigint unsigned NOT NULL DEFAULT 0 COMMENT '空间ID',
    `entry_id`          bigint unsigned NOT NULL DEFAULT 0 COMMENT '条目ID',
    `entry_type`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '条目类型:1-文件; 2-文件夹',
    `user_id`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '分享者ID',
    `link_name`         varchar(64) NOT NULL DEFAULT '' COMMENT '分享链接名称',
    `link_key`          varchar(64) NOT NULL DEFAULT '' COMMENT '分享链接唯一标识',
    `link_type`         tinyint unsigned NOT NULL DEFAULT 0 COMMENT '分享类型:1-公开链接; 2-加密链接',
    `access_code`       varchar(6) NOT NULL DEFAULT '' COMMENT '加密分享提取码',
    `is_deleted`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除; 1-已删除',
    `expired_at`        datetime DEFAULT NULL COMMENT '过期时间',
    `created_at`        datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_link_key` (`link_key`),
    KEY `idx_entry_user` (`entry_id`, `entry_type`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享表';


-- 用户表
CREATE TABLE `sys_user` (
    `id`            bigint unsigned NOT NULL COMMENT '用户ID',
    `username`      varchar(64) NOT NULL DEFAULT '' COMMENT '账号',
    `password`      varchar(128) NOT NULL DEFAULT '' COMMENT '密码',
    `avatar`        varchar(128) NOT NULL DEFAULT '' COMMENT '头像',
    `real_name`     varchar(32) NOT NULL DEFAULT '' COMMENT '真实姓名',
    `mobile`        varchar(16) NOT NULL DEFAULT '' COMMENT '手机号',    
    `email`         varchar(255) NOT NULL DEFAULT '' COMMENT '邮箱',
    `is_enabled`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用; 1-启用',
    `is_deleted`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除; 1-已删除',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    `updated_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`mobile`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_create` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 角色表
CREATE TABLE `sys_role` (
    `id`            bigint unsigned NOT NULL COMMENT '角色ID',
    `name`          varchar(64) NOT NULL DEFAULT '' COMMENT '角色名称',
    `code`          varchar(64) NOT NULL DEFAULT '' COMMENT '角色代码',
    `type`          tinyint unsigned NOT NULL DEFAULT 1 COMMENT '角色类型:1-全局角色; 2-组织内角色',
    `is_enabled`    tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用; 1-启用',
    `is_deleted`    tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除; 1-已删除',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    `updated_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


-- 权限表
CREATE TABLE `sys_perm` (
    `id`            bigint unsigned NOT NULL COMMENT '权限ID',
    `name`          varchar(64) NOT NULL DEFAULT '' COMMENT '权限名称',
    `code`          varchar(64) NOT NULL DEFAULT '' COMMENT '权限代码',
    `type`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '权限类型:1-菜单权限; 2-操作权限; 3-数据权限',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';


-- 用户-角色关联表
CREATE TABLE `sys_user_role` (
    `id`            bigint unsigned NOT NULL COMMENT '用户-角色关联ID',
    `user_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `role_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '角色ID',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';


-- 角色-权限关联表
CREATE TABLE `sys_role_perm` (
    `id`            bigint unsigned NOT NULL COMMENT '角色-权限关联ID',
    `role_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '角色ID',
    `perm_id`       bigint unsigned NOT NULL DEFAULT 0 COMMENT '权限ID',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator_id`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`),
    KEY `idx_perm_id` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';


-- 通知表
CREATE TABLE `sys_notice` (
    `id`            bigint unsigned NOT NULL COMMENT '通知记录ID',
    `title`         varchar(128) NOT NULL DEFAULT '' COMMENT '通知标题',
    `content`       varchar(512) NOT NULL DEFAULT '' COMMENT '通知内容',
    `type`          tinyint unsigned NOT NULL DEFAULT 1 COMMENT '通知类型: 1-系统公告; 2-系统告警',
    `target_id`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '接收者ID,为0则是全体用户',
    `is_read`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读; 1-已读',
    `is_deleted`    tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除; 1-已删除',
    `expired_at`    datetime DEFAULT NULL COMMENT '过期时间',
    `created_at`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_read` (`target_id`, `read`),
    KEY `idx_target_create` (`target_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';


-- 日志表
CREATE TABLE `sys_log` (
    `id`                bigint unsigned NOT NULL COMMENT '日志记录ID',
    `user_id`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
    `username`          varchar(64) NOT NULL DEFAULT '' COMMENT '用户名称',
    `request_uri`       varchar(128) NOT NULL DEFAULT '' COMMENT '请求路径',
    `request_params`    text COMMENT '请求参数(JSON)',
    `action_type`       varchar(32) NOT NULL DEFAULT '' COMMENT '操作: UPLOAD/DOWNLOAD/LOGIN/DELETE',
    `target_type`       varchar(32) NOT NULL DEFAULT '' COMMENT '对象类型: FILE/USER/SHARE/SYSTEM',
    `target_id`         bigint unsigned NOT NULL DEFAULT 0 COMMENT '对象ID',
    `target_name`       varchar(255) NOT NULL DEFAULT '' COMMENT '对象名称',
    `content`           text COMMENT '操作详情',
    `is_success`        tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否成功:0-失败; 1-成功',
    `error_msg`         text COMMENT '错误信息',
    `client_ip`         varchar(64) NOT NULL DEFAULT '' COMMENT 'IP地址', 
    `device_info`       varchar(255) NOT NULL DEFAULT '' COMMENT '设备信息',
    `created_at`        datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY idx_user_time (user_id, created_at),
    KEY idx_target_time (target_type, target_id, created_at),
    KEY idx_action_time (action_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表';