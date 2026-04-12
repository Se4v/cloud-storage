package org.example.backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        // 这里可以捕获所有未处理的异步异常
        logger.error("异步任务执行异常 - 方法名: {}, 参数: {}", method.getName(), params, ex);
    }
}
