package com.hiking.treasure.service;

import com.hiking.treasure.entity.QuartzJob;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 定时任务 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface QuartzJobService extends IService<QuartzJob> {
    boolean saveJob(QuartzJob job);
    boolean updateJob(QuartzJob job);
    boolean triggerNow(String id);
    void triggerDueJobs();
}
