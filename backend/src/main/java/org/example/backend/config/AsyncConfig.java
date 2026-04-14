package org.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {
    /**
     * 日志专用异步线程池
     * @return 配置好的线程池执行器
     */
    @Bean("logTaskExecutor") // 给你的线程池起个名字
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：始终存活的基础线程数量
        executor.setCorePoolSize(10);
        // 最大线程数：线程池允许创建的最大线程数量
        executor.setMaxPoolSize(20);
        // 队列容量：缓冲等待执行的任务数量
        executor.setQueueCapacity(1000);
        // 线程名前缀：方便日志排查定位线程
        executor.setThreadNamePrefix("Async-Log-");
        // 拒绝策略：队列满时，由调用方线程执行任务（保证日志不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}