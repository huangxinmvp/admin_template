package com.hiking.treasure.modules.phase1.service;

import com.hiking.treasure.modules.phase1.domain.dto.command.FigmaContextPreviewDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.LinearRepresentationPreviewDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.LinearWritePreviewDTO;
import com.hiking.treasure.modules.phase1.domain.vo.FigmaContextPreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.LinearRepresentationPreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.LinearWritePreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectToolBindingVO;
import com.hiking.treasure.modules.phase1.domain.vo.ToolIntegrationAuditVO;

import java.util.List;

public interface ToolIntegrationService {

    List<ProjectToolBindingVO> listProjectBindings(String projectId);

    List<ToolIntegrationAuditVO> listProjectAudits(String projectId, int limit);

    LinearRepresentationPreviewVO previewLinearProjectRepresentation(String projectId, LinearRepresentationPreviewDTO dto);

    ProjectToolBindingVO applyLinearProjectRepresentation(String projectId, LinearRepresentationPreviewDTO dto);

    LinearWritePreviewVO previewClarificationLinearWrite(String clarificationItemId, LinearWritePreviewDTO dto);

    ToolIntegrationAuditVO applyClarificationLinearWrite(String clarificationItemId, LinearWritePreviewDTO dto);

    LinearWritePreviewVO previewDecisionLinearWrite(String decisionItemId, LinearWritePreviewDTO dto);

    ToolIntegrationAuditVO applyDecisionLinearWrite(String decisionItemId, LinearWritePreviewDTO dto);

    FigmaContextPreviewVO previewFigmaContext(String projectId, FigmaContextPreviewDTO dto);

    ProjectToolBindingVO applyFigmaContext(String projectId, FigmaContextPreviewDTO dto);
}
