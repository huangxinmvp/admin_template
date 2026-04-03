package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;

import java.util.List;

public interface ClarificationItemService extends IService<ClarificationItem> {
    List<ClarificationItem> generateMockItems(String projectId);
}
