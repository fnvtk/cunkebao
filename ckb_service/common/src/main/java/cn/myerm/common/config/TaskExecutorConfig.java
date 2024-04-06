package cn.myerm.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class TaskExecutorConfig implements SchedulingConfigurer {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler1() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("Scheduler1-");
        scheduler.setPoolSize(20);
        return scheduler;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler2() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("Scheduler2-");
        scheduler.setPoolSize(10);
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler1()); // 默认使用taskScheduler1作为任务执行器
    }
}

