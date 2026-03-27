package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.entity.QuartzJob;
import com.hiking.treasure.entity.QuartzJobLog;
import com.hiking.treasure.mapper.QuartzJobMapper;
import com.hiking.treasure.service.QuartzJobLogService;
import com.hiking.treasure.service.QuartzJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * <p>
 * 定时任务 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
@RequiredArgsConstructor
public class QuartzJobServiceImpl extends ServiceImpl<QuartzJobMapper, QuartzJob> implements QuartzJobService {

    private final QuartzJobLogService quartzJobLogService;
    private final ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveJob(QuartzJob job) {
        normalizeJob(job);
        return save(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(QuartzJob job) {
        normalizeJob(job);
        return updateById(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerNow(String id) {
        QuartzJob job = getById(id);
        if (job == null) {
            throw new BusinessException(404, "任务不存在");
        }
        execute(job, "MANUAL");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerDueJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<QuartzJob> dueJobs = list(new LambdaQueryWrapper<QuartzJob>()
                .eq(QuartzJob::getStatus, 1)
                .le(QuartzJob::getNextRunTime, now));
        for (QuartzJob job : dueJobs) {
            execute(job, "SCHEDULED");
        }
    }

    private void normalizeJob(QuartzJob job) {
        if (!StringUtils.hasText(job.getCronExpression())) {
            throw new BusinessException(400, "Cron 表达式不能为空");
        }
        CronExpression expression = CronExpression.parse(job.getCronExpression());
        job.setNextRunTime(LocalDateTime.ofInstant(expression.next(java.time.ZonedDateTime.now()).toInstant(), ZoneId.systemDefault()));
        if (job.getStatus() == null) {
            job.setStatus(1);
        }
        if (job.getRunCount() == null) {
            job.setRunCount(0L);
        }
    }

    private void execute(QuartzJob job, String triggerType) {
        LocalDateTime start = LocalDateTime.now();
        long begin = System.currentTimeMillis();
        QuartzJobLog jobLog = new QuartzJobLog();
        jobLog.setJobId(job.getId());
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setCronExpression(job.getCronExpression());
        jobLog.setTriggerType(triggerType);
        try {
            invokeTarget(job.getInvokeTarget());
            long cost = System.currentTimeMillis() - begin;
            job.setLastRunTime(start);
            job.setRunCount((job.getRunCount() == null ? 0L : job.getRunCount()) + 1);
            job.setLastStatus(1);
            job.setLastMessage("SUCCESS");
            job.setNextRunTime(nextRunTime(job.getCronExpression(), start));
            updateById(job);

            jobLog.setStatus(1);
            jobLog.setCostTime(cost);
            quartzJobLogService.save(jobLog);
        } catch (Exception ex) {
            long cost = System.currentTimeMillis() - begin;
            job.setLastRunTime(start);
            job.setRunCount((job.getRunCount() == null ? 0L : job.getRunCount()) + 1);
            job.setLastStatus(0);
            job.setLastMessage(ex.getMessage());
            job.setNextRunTime(nextRunTime(job.getCronExpression(), start));
            updateById(job);

            jobLog.setStatus(0);
            jobLog.setCostTime(cost);
            jobLog.setExceptionInfo(ex.getMessage());
            quartzJobLogService.save(jobLog);
            throw new BusinessException(500, "任务执行失败: " + ex.getMessage());
        }
    }

    private void invokeTarget(String invokeTarget) throws Exception {
        if (!StringUtils.hasText(invokeTarget) || !invokeTarget.contains("#")) {
            throw new BusinessException(400, "调用目标格式应为 beanName#methodName");
        }
        String[] parts = invokeTarget.split("#", 2);
        Object bean = applicationContext.getBean(parts[0]);
        Method method = bean.getClass().getMethod(parts[1]);
        method.invoke(bean);
    }

    private LocalDateTime nextRunTime(String cronExpression, LocalDateTime baseTime) {
        CronExpression expression = CronExpression.parse(cronExpression);
        java.time.ZonedDateTime next = expression.next(baseTime.atZone(ZoneId.systemDefault()));
        return next == null ? null : next.toLocalDateTime();
    }
}
