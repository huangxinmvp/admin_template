package com.hiking.treasure.component;

import com.hiking.treasure.service.QuartzJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.quartz.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class QuartzJobScheduler {

    private final QuartzJobService quartzJobService;

    @Scheduled(fixedDelay = 60000)
    public void scanDueJobs() {
        try {
            quartzJobService.triggerDueJobs();
        } catch (Exception ex) {
            log.error("扫描到期任务失败", ex);
        }
    }
}
