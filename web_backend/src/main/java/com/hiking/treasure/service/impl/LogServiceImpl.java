package com.hiking.treasure.service.impl;

import com.hiking.treasure.entity.Log;
import com.hiking.treasure.mapper.LogMapper;
import com.hiking.treasure.service.LogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统日志 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {

}
