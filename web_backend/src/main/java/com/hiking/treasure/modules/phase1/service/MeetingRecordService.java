package com.hiking.treasure.modules.phase1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hiking.treasure.modules.phase1.entity.MeetingRecord;

import java.util.List;

public interface MeetingRecordService extends IService<MeetingRecord> {

    List<MeetingRecord> listByProjectId(String projectId);

    MeetingRecord saveGeneratedRecord(String projectId, MeetingRecord record);
}
