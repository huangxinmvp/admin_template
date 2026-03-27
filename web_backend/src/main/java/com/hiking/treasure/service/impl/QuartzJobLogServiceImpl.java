package com.hiking.treasure.service.impl;

import com.hiking.treasure.entity.QuartzJobLog;
import com.hiking.treasure.mapper.QuartzJobLogMapper;
import com.hiking.treasure.service.QuartzJobLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 定时任务执行日志 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
public class QuartzJobLogServiceImpl extends ServiceImpl<QuartzJobLogMapper, QuartzJobLog> implements QuartzJobLogService {

}
