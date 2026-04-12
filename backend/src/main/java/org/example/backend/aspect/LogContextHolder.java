package org.example.backend.aspect;

import java.util.HashMap;
import java.util.Map;

public class LogContextHolder {
    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void setTargetId(Long targetId) {
        CONTEXT.get().put("targetId", targetId);
    }

    public static void setTargetName(String targetName) {
        CONTEXT.get().put("targetName", targetName);
    }

    public static void setDetail(String detail) {
        CONTEXT.get().put("detail", detail);
    }

    /**
     * 追加额外属性到 detail 中 (存入 Map，切面会将其转为 JSON)
     */
    public static void addDetailProperty(String key, Object value) {
        Map<String, Object> map = CONTEXT.get();
        map.put(key, value);
    }

    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    public static Map<String, Object> getAll() {
        return CONTEXT.get();
    }

    /**
     * 【极其重要】防内存泄漏，必须在请求结束后清除
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
