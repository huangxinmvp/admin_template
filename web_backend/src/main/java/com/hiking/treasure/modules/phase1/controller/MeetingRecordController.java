package com.hiking.treasure.modules.phase1.controller;

import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.modules.phase1.domain.convert.MeetingRecordConvert;
import com.hiking.treasure.modules.phase1.domain.dto.command.MeetingRecordSaveDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.MeetingSummaryGenerateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingRecordVO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingSummarySuggestionVO;
import com.hiking.treasure.modules.phase1.domain.dto.command.ProductArchitectureBriefGenerateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ProductArchitectureBriefVO;
import com.hiking.treasure.modules.phase1.entity.MeetingRecord;
import com.hiking.treasure.modules.phase1.service.AgentRuntimeSuggestionService;
import com.hiking.treasure.modules.phase1.service.MeetingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "AICoOS 会议记录", description = "AICoOS 项目会议总结与记录接口")
@RestController
@RequestMapping("/api/aicoos/meetingRecord")
@PreAuthorize("hasRole('ADMIN')")
public class MeetingRecordController {

    @Resource
    private MeetingRecordService meetingRecordService;

    @Resource
    private MeetingRecordConvert meetingRecordConvert;

    @Resource
    private AgentRuntimeSuggestionService agentRuntimeSuggestionService;

    @Operation(summary = "按项目查询会议记录")
    @GetMapping("/project/{projectId}")
    public Result<List<MeetingRecordVO>> listByProjectId(@PathVariable String projectId) {
        return Result.ok(meetingRecordConvert.toVOs(meetingRecordService.listByProjectId(projectId)));
    }

    @Operation(summary = "生成会议总结建议")
    @PostMapping("/project/{projectId}/summary-suggestion")
    public Result<MeetingSummarySuggestionVO> generateMeetingSummary(
            @PathVariable String projectId,
            @Valid @RequestBody MeetingSummaryGenerateDTO dto) {
        return Result.ok(agentRuntimeSuggestionService.generateMeetingSummary(projectId, dto));
    }

    @Operation(summary = "生成产品与架构协作简报")
    @PostMapping("/project/{projectId}/product-architecture-brief")
    public Result<ProductArchitectureBriefVO> generateProductArchitectureBrief(
            @PathVariable String projectId,
            @RequestBody(required = false) ProductArchitectureBriefGenerateDTO dto) {
        return Result.ok(agentRuntimeSuggestionService.generateProductArchitectureBrief(projectId, dto));
    }

    @Operation(summary = "保存会议记录")
    @PostMapping("/project/{projectId}")
    public Result<MeetingRecordVO> saveMeetingRecord(
            @PathVariable String projectId,
            @Valid @RequestBody MeetingRecordSaveDTO dto) {
        MeetingRecord record = meetingRecordConvert.toEntity(dto);
        record.setProjectId(projectId);
        record.setGeneratedBy(SecurityUtils.getUserId());
        record.setGeneratedAt(LocalDateTime.now());
        return Result.ok(meetingRecordConvert.toVO(meetingRecordService.saveGeneratedRecord(projectId, record)));
    }
}
