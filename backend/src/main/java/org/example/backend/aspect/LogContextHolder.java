package org.example.backend.aspect;

import java.util.HashMap;
import java.util.Map;

public class LogContextHolder {
    /** 线程本地变量 */
    private static final ThreadLocal<Map<String, Object>> LOG_CONTEXT = ThreadLocal.withInitial(HashMap::new);

    /**
     * 设置操作目标ID
     * @param targetId 业务目标ID
     */
    public static void setTargetId(Long targetId) {
        LOG_CONTEXT.get().put("targetId", targetId);
    }

    /**
     * 设置操作目标名称
     * @param targetName 业务目标名称
     */
    public static void setTargetName(String targetName) {
        LOG_CONTEXT.get().put("targetName", targetName);
    }

    /**
     * 设置操作日志详情
     * @param detail 日志详情内容
     */
    public static void setDetail(String detail) {
        LOG_CONTEXT.get().put("detail", detail);
    }

    /**
     * 追加自定义扩展属性到日志上下文
     * @param key 扩展属性键
     * @param value 扩展属性值
     */
    public static void addDetailProperty(String key, Object value) {
        Map<String, Object> map = LOG_CONTEXT.get();
        map.put(key, value);
    }

    /**
     * 根据key获取上下文数据
     * @param key 数据键名
     * @return 对应的数据值
     */
    public static Object get(String key) {
        return LOG_CONTEXT.get().get(key);
    }

    /**
     * 获取当前线程所有日志上下文数据
     * @return 上下文数据集合
     */
    public static Map<String, Object> getAll() {
        return LOG_CONTEXT.get();
    }

    /**
     * 清空当前线程的上下文数据
     */
    public static void clear() {
        LOG_CONTEXT.remove();
    }
}
