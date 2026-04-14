package org.example.backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

    /**
     * 异步任务未捕获异常处理
     * @param ex 异常对象
     * @param method 发生异常的方法
     * @param params 方法入参
     */
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        // 这里可以捕获所有未处理的异步异常
        logger.error("异步任务执行异常 - 方法名: {}, 参数: {}", method.getName(), params, ex);
    }
}
