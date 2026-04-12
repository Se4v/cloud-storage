package org.example.backend.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 所属模块 (如: 文件管理, 分享大厅, 回收站)
     */
    String module() default "";

    /**
     * 操作类型，如：UPLOAD, DELETE, LOGIN
     */
    String action() default "";

    /**
     * 操作对象类型，如：FILE, USER, SYSTEM
     */
    String targetType() default "";

    /**
     * 目标对象ID (支持 SpEL 表达式，如 "#fileId" 或 "#user.id")
     */
    String targetId() default "";

    /**
     * 目标对象名称 (支持 SpEL 表达式，如 "#fileName" 或 "#user.name")
     */
    String targetName() default "";

    /**
     * 操作详情描述
     */
    String detail() default "";
}
