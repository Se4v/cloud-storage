package org.example.backend.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /** 所属模块 */
    String module() default "";
    /** 操作类型 */
    String action() default "";
    /** 操作对象类型 */
    String targetType() default "";
    /** 目标对象ID */
    String targetId() default "";
    /** 目标对象名称 */
    String targetName() default "";
    /** 操作详情描述 */
    String detail() default "";
}