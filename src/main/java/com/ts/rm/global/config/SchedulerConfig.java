package com.ts.rm.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * Scheduler Configuration
 *
 * <p>동적 스케줄러를 위한 TaskScheduler 설정
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * 스케줄러용 스레드 풀
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(5);
    }

    /**
     * TaskScheduler (동적 스케줄링용)
     */
    @Bean
    public TaskScheduler taskScheduler(ScheduledExecutorService scheduledExecutorService) {
        return new ConcurrentTaskScheduler(scheduledExecutorService);
    }
}
