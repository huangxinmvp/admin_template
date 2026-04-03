package com.hiking.treasure.modules.phase1.domain.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.modules.phase1.domain.dto.command.MeetingRecordSaveDTO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingDecisionCandidateVO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingRecordVO;
import com.hiking.treasure.modules.phase1.entity.MeetingRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingRecordConvert {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MeetingRecord toEntity(MeetingRecordSaveDTO dto) {
        if (dto == null) {
            return null;
        }
        MeetingRecord entity = new MeetingRecord();
        entity.setMeetingTitle(dto.getMeetingTitle());
        entity.setRawNotes(dto.getRawNotes());
        entity.setSummary(dto.getSummary());
        entity.setActionItemsJson(toJson(dto.getActionItems()));
        entity.setOpenQuestionsJson(toJson(dto.getOpenQuestions()));
        entity.setDecisionCandidatesJson(toJson(dto.getDecisionCandidates()));
        entity.setSourceType(dto.getSourceType());
        entity.setSourceObjectId(dto.getSourceObjectId());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    public MeetingRecordVO toVO(MeetingRecord entity) {
        if (entity == null) {
            return null;
        }
        MeetingRecordVO vo = new MeetingRecordVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setMeetingTitle(entity.getMeetingTitle());
        vo.setRawNotes(entity.getRawNotes());
        vo.setSummary(entity.getSummary());
        vo.setActionItems(readStringList(entity.getActionItemsJson()));
        vo.setOpenQuestions(readStringList(entity.getOpenQuestionsJson()));
        vo.setDecisionCandidates(readDecisionCandidates(entity.getDecisionCandidatesJson()));
        vo.setSourceType(entity.getSourceType());
        vo.setSourceObjectId(entity.getSourceObjectId());
        vo.setGeneratedBy(entity.getGeneratedBy());
        vo.setGeneratedAt(entity.getGeneratedAt());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    public List<MeetingRecordVO> toVOs(List<MeetingRecord> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toVO).toList();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> readStringList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<MeetingDecisionCandidateVO> readDecisionCandidates(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<MeetingDecisionCandidateVO>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }
}
