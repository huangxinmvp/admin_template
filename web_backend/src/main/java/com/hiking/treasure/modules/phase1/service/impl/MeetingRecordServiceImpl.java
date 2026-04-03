package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.entity.MeetingRecord;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.mapper.MeetingRecordMapper;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.service.MeetingRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingRecordServiceImpl extends ServiceImpl<MeetingRecordMapper, MeetingRecord>
        implements MeetingRecordService {

    private final ProjectMapper projectMapper;

    @Override
    public List<MeetingRecord> listByProjectId(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<MeetingRecord>()
                .eq(MeetingRecord::getProjectId, projectId)
                .orderByDesc(MeetingRecord::getGeneratedAt)
                .orderByDesc(MeetingRecord::getCreateTime));
    }

    @Override
    public MeetingRecord saveGeneratedRecord(String projectId, MeetingRecord record) {
        if (projectId == null || projectId.isBlank()) {
            throw new BusinessException(400, "项目ID不能为空");
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        record.setProjectId(projectId);
        save(record);
        return getById(record.getId());
    }
}
