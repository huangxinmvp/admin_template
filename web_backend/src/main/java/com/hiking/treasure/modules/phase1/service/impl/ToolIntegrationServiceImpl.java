package com.hiking.treasure.modules.phase1.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.modules.phase1.domain.convert.ProjectToolBindingConvert;
import com.hiking.treasure.modules.phase1.domain.convert.ToolIntegrationAuditConvert;
import com.hiking.treasure.modules.phase1.domain.dto.command.FigmaContextPreviewDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.LinearRepresentationPreviewDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.LinearWritePreviewDTO;
import com.hiking.treasure.modules.phase1.domain.vo.FigmaContextPreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.LinearRepresentationPreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.LinearWritePreviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectToolBindingVO;
import com.hiking.treasure.modules.phase1.domain.vo.ToolIntegrationAuditVO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.ProjectToolBinding;
import com.hiking.treasure.modules.phase1.entity.ToolIntegrationAudit;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ExternalToolType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.LinearWriteMode;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectToolBindingStatus;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ProjectToolBindingType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ToolIntegrationActionType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ToolIntegrationAuditStatus;
import com.hiking.treasure.modules.phase1.integration.FigmaIntegrationClient;
import com.hiking.treasure.modules.phase1.integration.LinearIntegrationClient;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadResponse;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateResponse;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateResponse;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.DecisionItemService;
import com.hiking.treasure.modules.phase1.service.ProjectService;
import com.hiking.treasure.modules.phase1.service.ProjectToolBindingService;
import com.hiking.treasure.modules.phase1.service.ToolIntegrationAuditService;
import com.hiking.treasure.modules.phase1.service.ToolIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ToolIntegrationServiceImpl implements ToolIntegrationService {

    private final ProjectService projectService;
    private final ClarificationItemService clarificationItemService;
    private final DecisionItemService decisionItemService;
    private final ProjectToolBindingService projectToolBindingService;
    private final ToolIntegrationAuditService toolIntegrationAuditService;
    private final ProjectToolBindingConvert projectToolBindingConvert;
    private final ToolIntegrationAuditConvert toolIntegrationAuditConvert;
    private final LinearIntegrationClient linearIntegrationClient;
    private final FigmaIntegrationClient figmaIntegrationClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProjectToolBindingVO> listProjectBindings(String projectId) {
        return projectToolBindingConvert.toVOs(projectToolBindingService.listByProjectId(projectId));
    }

    @Override
    public List<ToolIntegrationAuditVO> listProjectAudits(String projectId, int limit) {
        return toolIntegrationAuditConvert.toVOs(toolIntegrationAuditService.listByProjectId(projectId, limit));
    }

    @Override
    public LinearRepresentationPreviewVO previewLinearProjectRepresentation(String projectId, LinearRepresentationPreviewDTO dto) {
        Project project = requireProject(projectId);
        LinearRepresentationPreviewVO preview = buildLinearProjectRepresentationPreview(project, dto);
        recordPreviewAudit(
                projectId,
                ExternalToolType.LINEAR.getCode(),
                ToolIntegrationActionType.LINEAR_PROJECT_PREVIEW.getCode(),
                "project",
                projectId,
                dto,
                preview
        );
        return preview;
    }

    @Override
    public ProjectToolBindingVO applyLinearProjectRepresentation(String projectId, LinearRepresentationPreviewDTO dto) {
        Project project = requireProject(projectId);
        LinearRepresentationPreviewVO preview = buildLinearProjectRepresentationPreview(project, dto);
        try {
            ProjectToolBinding binding = new ProjectToolBinding();
            binding.setProjectId(projectId);
            binding.setToolType(ExternalToolType.LINEAR.getCode());
            binding.setBindingType(ProjectToolBindingType.LINEAR_PRIMARY_ISSUE.getCode());
            binding.setBindingStatus(ProjectToolBindingStatus.LINKED.getCode());
            binding.setDefaultFlag(1);
            binding.setRemark(trim(dto.getRemark()));
            if (Objects.equals(preview.getMode(), "link_existing")) {
                binding.setExternalId(trim(preview.getExistingIssueId()));
                binding.setExternalKey(firstNonBlank(preview.getExistingIssueIdentifier(), preview.getExistingIssueId()));
                binding.setExternalName(preview.getRepresentationTitle());
                binding.setExternalUrl(trim(preview.getExistingIssueUrl()));
                binding.setMetadataJson(writeJson(preview));
            } else {
                LinearIssueCreateRequest request = new LinearIssueCreateRequest();
                request.setTeamId(preview.getTeamId());
                request.setTitle(preview.getRepresentationTitle());
                request.setDescription(preview.getRepresentationDescription());
                LinearIssueCreateResponse response = linearIntegrationClient.createIssue(request);
                binding.setExternalId(response.getId());
                binding.setExternalKey(firstNonBlank(response.getIdentifier(), response.getId()));
                binding.setExternalName(firstNonBlank(response.getTitle(), preview.getRepresentationTitle()));
                binding.setExternalUrl(response.getUrl());
                binding.setMetadataJson(writeJson(response));
            }
            ProjectToolBinding saved = projectToolBindingService.saveOrReplaceDefaultBinding(binding);
            recordApplyAuditSuccess(
                    projectId,
                    ExternalToolType.LINEAR.getCode(),
                    ToolIntegrationActionType.LINEAR_PROJECT_APPLY.getCode(),
                    "project",
                    projectId,
                    saved.getId(),
                    dto,
                    saved.getExternalId(),
                    saved.getExternalUrl(),
                    saved
            );
            return projectToolBindingConvert.toVO(saved);
        } catch (Exception ex) {
            recordApplyAuditFailure(
                    projectId,
                    ExternalToolType.LINEAR.getCode(),
                    ToolIntegrationActionType.LINEAR_PROJECT_APPLY.getCode(),
                    "project",
                    projectId,
                    dto,
                    ex
            );
            throw ex;
        }
    }

    @Override
    public LinearWritePreviewVO previewClarificationLinearWrite(String clarificationItemId, LinearWritePreviewDTO dto) {
        ClarificationItem clarificationItem = clarificationItemService.getById(clarificationItemId);
        if (clarificationItem == null) {
            throw new BusinessException(404, "澄清项不存在");
        }
        LinearWritePreviewVO preview = buildClarificationLinearPreview(clarificationItem, dto);
        recordPreviewAudit(
                clarificationItem.getProjectId(),
                ExternalToolType.LINEAR.getCode(),
                actionTypeForWriteMode(preview.getWriteMode(), true),
                "clarification_item",
                clarificationItemId,
                dto,
                preview
        );
        return preview;
    }

    @Override
    public ToolIntegrationAuditVO applyClarificationLinearWrite(String clarificationItemId, LinearWritePreviewDTO dto) {
        ClarificationItem clarificationItem = clarificationItemService.getById(clarificationItemId);
        if (clarificationItem == null) {
            throw new BusinessException(404, "澄清项不存在");
        }
        LinearWritePreviewVO preview = buildClarificationLinearPreview(clarificationItem, dto);
        return executeLinearWrite(
                clarificationItem.getProjectId(),
                "clarification_item",
                clarificationItemId,
                preview,
                dto
        );
    }

    @Override
    public LinearWritePreviewVO previewDecisionLinearWrite(String decisionItemId, LinearWritePreviewDTO dto) {
        DecisionItem decisionItem = decisionItemService.getById(decisionItemId);
        if (decisionItem == null) {
            throw new BusinessException(404, "决策事项不存在");
        }
        LinearWritePreviewVO preview = buildDecisionLinearPreview(decisionItem, dto);
        recordPreviewAudit(
                decisionItem.getProjectId(),
                ExternalToolType.LINEAR.getCode(),
                actionTypeForWriteMode(preview.getWriteMode(), true),
                "decision_item",
                decisionItemId,
                dto,
                preview
        );
        return preview;
    }

    @Override
    public ToolIntegrationAuditVO applyDecisionLinearWrite(String decisionItemId, LinearWritePreviewDTO dto) {
        DecisionItem decisionItem = decisionItemService.getById(decisionItemId);
        if (decisionItem == null) {
            throw new BusinessException(404, "决策事项不存在");
        }
        LinearWritePreviewVO preview = buildDecisionLinearPreview(decisionItem, dto);
        return executeLinearWrite(
                decisionItem.getProjectId(),
                "decision_item",
                decisionItemId,
                preview,
                dto
        );
    }

    @Override
    public FigmaContextPreviewVO previewFigmaContext(String projectId, FigmaContextPreviewDTO dto) {
        requireProject(projectId);
        FigmaContextPreviewVO preview = buildFigmaContextPreview(projectId, dto);
        recordPreviewAudit(
                projectId,
                ExternalToolType.FIGMA.getCode(),
                ToolIntegrationActionType.FIGMA_CONTEXT_PREVIEW.getCode(),
                "project",
                projectId,
                dto,
                preview
        );
        return preview;
    }

    @Override
    public ProjectToolBindingVO applyFigmaContext(String projectId, FigmaContextPreviewDTO dto) {
        requireProject(projectId);
        FigmaContextPreviewVO preview = buildFigmaContextPreview(projectId, dto);
        try {
            ProjectToolBinding binding = new ProjectToolBinding();
            binding.setProjectId(projectId);
            binding.setToolType(ExternalToolType.FIGMA.getCode());
            binding.setBindingType(preview.getBindingType());
            binding.setExternalId(firstNonBlank(preview.getNodeId(), preview.getFileKey()));
            binding.setExternalKey(preview.getFileKey());
            binding.setExternalName(preview.getBindingName());
            binding.setExternalUrl(firstNonBlank(preview.getExternalUrl(), preview.getFigmaUrl()));
            binding.setBindingStatus(ProjectToolBindingStatus.LINKED.getCode());
            binding.setDefaultFlag(Objects.equals(preview.getBindingType(), ProjectToolBindingType.FIGMA_FILE.getCode()) ? 1 : 0);
            binding.setMetadataJson(preview.getMetadataJson());
            binding.setRemark(trim(dto.getRemark()));
            ProjectToolBinding saved = savedBinding(binding);
            recordApplyAuditSuccess(
                    projectId,
                    ExternalToolType.FIGMA.getCode(),
                    ToolIntegrationActionType.FIGMA_CONTEXT_APPLY.getCode(),
                    "project",
                    projectId,
                    saved.getId(),
                    dto,
                    saved.getExternalId(),
                    saved.getExternalUrl(),
                    saved
            );
            return projectToolBindingConvert.toVO(saved);
        } catch (Exception ex) {
            recordApplyAuditFailure(
                    projectId,
                    ExternalToolType.FIGMA.getCode(),
                    ToolIntegrationActionType.FIGMA_CONTEXT_APPLY.getCode(),
                    "project",
                    projectId,
                    dto,
                    ex
            );
            throw ex;
        }
    }

    private LinearRepresentationPreviewVO buildLinearProjectRepresentationPreview(Project project, LinearRepresentationPreviewDTO dto) {
        LinearRepresentationPreviewVO preview = new LinearRepresentationPreviewVO();
        preview.setMode(textOrDefault(dto.getMode(), "create"));
        preview.setProjectId(project.getId());
        preview.setTeamId(dto.getTeamId());
        if (Objects.equals(preview.getMode(), "link_existing")) {
            preview.setExistingIssueId(trim(dto.getExistingIssueId()));
            preview.setExistingIssueIdentifier(trim(dto.getExistingIssueIdentifier()));
            preview.setExistingIssueUrl(trim(dto.getExistingIssueUrl()));
            preview.setRepresentationTitle(firstNonBlank(dto.getRepresentationTitle(), project.getProjectName()));
            preview.setRepresentationDescription(firstNonBlank(
                    dto.getRepresentationDescription(),
                    "Link existing Linear issue to AICoOS project " + firstNonBlank(project.getProjectName(), project.getProjectCode(), project.getId())
            ));
            preview.setOperationSummary("将把现有 Linear 工作项链接为该项目的外部主工作项，不会自动修改 Linear 内容。");
        } else {
            preview.setRepresentationTitle(firstNonBlank(
                    dto.getRepresentationTitle(),
                    buildLinearProjectRepresentationTitle(project)
            ));
            preview.setRepresentationDescription(firstNonBlank(
                    dto.getRepresentationDescription(),
                    buildLinearProjectRepresentationDescription(project)
            ));
            preview.setOperationSummary("将向 Linear 创建一个主工作项作为该项目的受控外部映射。");
        }
        return preview;
    }

    private FigmaContextPreviewVO buildFigmaContextPreview(String projectId, FigmaContextPreviewDTO dto) {
        FigmaParseResult parseResult = resolveFigmaTarget(dto);
        FigmaContextReadRequest request = new FigmaContextReadRequest();
        request.setFileKey(parseResult.fileKey());
        request.setNodeId(parseResult.nodeId());
        FigmaContextReadResponse readResponse = figmaIntegrationClient.readContext(request);

        FigmaContextPreviewVO preview = new FigmaContextPreviewVO();
        preview.setProjectId(projectId);
        preview.setBindingType(readResponse.getNodeId() == null || readResponse.getNodeId().isBlank()
                ? ProjectToolBindingType.FIGMA_FILE.getCode()
                : ProjectToolBindingType.FIGMA_NODE.getCode());
        preview.setBindingName(firstNonBlank(dto.getBindingName(), readResponse.getNodeName(), readResponse.getFileName()));
        preview.setFigmaUrl(firstNonBlank(trim(dto.getFigmaUrl()), readResponse.getExternalUrl()));
        preview.setFileKey(readResponse.getFileKey());
        preview.setFileName(readResponse.getFileName());
        preview.setNodeId(readResponse.getNodeId());
        preview.setNodeName(readResponse.getNodeName());
        preview.setExternalUrl(readResponse.getExternalUrl());
        preview.setLastModifiedAt(readResponse.getLastModifiedAt());
        preview.setMetadataJson(readResponse.getMetadataJson());
        preview.setOperationSummary(readResponse.getNodeId() == null || readResponse.getNodeId().isBlank()
                ? "将把 Figma 文件上下文链接到项目详情。"
                : "将把 Figma 节点上下文链接到项目详情。");
        return preview;
    }

    private ToolIntegrationAuditVO executeLinearWrite(
            String projectId,
            String sourceObjectType,
            String sourceObjectId,
            LinearWritePreviewVO preview,
            LinearWritePreviewDTO requestDto) {
        ProjectToolBinding primaryBinding = resolveLinearPrimaryBinding(projectId);
        try {
            String externalId;
            String externalUrl;
            if (Objects.equals(preview.getWriteMode(), LinearWriteMode.COMMENT.getCode())) {
                LinearCommentCreateRequest request = new LinearCommentCreateRequest();
                request.setIssueId(preview.getTargetIssueId());
                request.setBody(preview.getBody());
                LinearCommentCreateResponse response = linearIntegrationClient.createComment(request);
                externalId = response.getId();
                externalUrl = response.getUrl();
            } else {
                LinearIssueCreateRequest request = new LinearIssueCreateRequest();
                request.setTeamId(preview.getTeamId());
                request.setTitle(preview.getTitle());
                request.setDescription(preview.getBody());
                LinearIssueCreateResponse response = linearIntegrationClient.createIssue(request);
                externalId = response.getId();
                externalUrl = response.getUrl();
            }
            ToolIntegrationAudit audit = recordApplyAuditSuccess(
                    projectId,
                    ExternalToolType.LINEAR.getCode(),
                    actionTypeForWriteMode(preview.getWriteMode(), false),
                    sourceObjectType,
                    sourceObjectId,
                    primaryBinding == null ? null : primaryBinding.getId(),
                    requestDto,
                    externalId,
                    externalUrl,
                    preview
            );
            return toolIntegrationAuditConvert.toVO(audit);
        } catch (Exception ex) {
            recordApplyAuditFailure(
                    projectId,
                    ExternalToolType.LINEAR.getCode(),
                    actionTypeForWriteMode(preview.getWriteMode(), false),
                    sourceObjectType,
                    sourceObjectId,
                    requestDto,
                    ex
            );
            throw ex;
        }
    }

    private LinearWritePreviewVO buildClarificationLinearPreview(ClarificationItem clarificationItem, LinearWritePreviewDTO dto) {
        LinearWritePreviewVO preview = new LinearWritePreviewVO();
        preview.setToolType(ExternalToolType.LINEAR.getCode());
        preview.setSourceObjectType("clarification_item");
        preview.setSourceObjectId(clarificationItem.getId());
        preview.setProjectId(clarificationItem.getProjectId());
        preview.setWriteMode(normalizeWriteMode(dto.getWriteMode()));
        preview.setTeamId(trim(dto.getTeamId()));
        if (Objects.equals(preview.getWriteMode(), LinearWriteMode.COMMENT.getCode())) {
            ProjectToolBinding binding = resolveLinearPrimaryBinding(clarificationItem.getProjectId());
            preview.setTargetIssueId(firstNonBlank(trim(dto.getTargetIssueId()), binding == null ? null : binding.getExternalId()));
            preview.setTargetIssueIdentifier(firstNonBlank(trim(dto.getTargetIssueIdentifier()), binding == null ? null : binding.getExternalKey()));
            preview.setTargetIssueUrl(firstNonBlank(trim(dto.getTargetIssueUrl()), binding == null ? null : binding.getExternalUrl()));
            if (preview.getTargetIssueId() == null || preview.getTargetIssueId().isBlank()) {
                throw new BusinessException(400, "当前项目还没有可用于评论的 Linear 主工作项。请先在项目中心建立 Linear 项目映射，或切换为 Issue 模式");
            }
            preview.setBody(firstNonBlank(
                    trim(dto.getBody()),
                    """
                    [AICoOS 澄清项]
                    标题：%s
                    问题：%s
                    分类：%s
                    严重等级：%s
                    建议选项：%s
                    当前状态：%s
                    """.formatted(
                            firstNonBlank(clarificationItem.getTitle(), "-"),
                            firstNonBlank(clarificationItem.getQuestion(), "-"),
                            firstNonBlank(clarificationItem.getCategory(), "-"),
                            firstNonBlank(clarificationItem.getSeverity(), "-"),
                            firstNonBlank(clarificationItem.getSuggestedOptions(), "-"),
                            firstNonBlank(clarificationItem.getStatus(), "-")
                    ).trim()
            ));
            preview.setOperationSummary("将把澄清项作为 Comment 写入该项目的 Linear 主工作项。");
        } else {
            preview.setTitle(firstNonBlank(
                    trim(dto.getTitle()),
                    "[AICoOS 澄清] " + firstNonBlank(clarificationItem.getTitle(), clarificationItem.getQuestion(), clarificationItem.getId())
            ));
            preview.setBody(firstNonBlank(
                    trim(dto.getBody()),
                    """
                    ## AICoOS 澄清项

                    - 问题：%s
                    - 分类：%s
                    - 严重等级：%s
                    - 建议选项：%s
                    - 用户回复：%s
                    - 当前状态：%s
                    """.formatted(
                            firstNonBlank(clarificationItem.getQuestion(), "-"),
                            firstNonBlank(clarificationItem.getCategory(), "-"),
                            firstNonBlank(clarificationItem.getSeverity(), "-"),
                            firstNonBlank(clarificationItem.getSuggestedOptions(), "-"),
                            firstNonBlank(clarificationItem.getUserResponse(), "-"),
                            firstNonBlank(clarificationItem.getStatus(), "-")
                    ).trim()
            ));
            preview.setOperationSummary("将把澄清项作为独立 Linear Issue 写入外部交付工具。");
        }
        return preview;
    }

    private LinearWritePreviewVO buildDecisionLinearPreview(DecisionItem decisionItem, LinearWritePreviewDTO dto) {
        LinearWritePreviewVO preview = new LinearWritePreviewVO();
        preview.setToolType(ExternalToolType.LINEAR.getCode());
        preview.setSourceObjectType("decision_item");
        preview.setSourceObjectId(decisionItem.getId());
        preview.setProjectId(decisionItem.getProjectId());
        preview.setWriteMode(normalizeWriteMode(dto.getWriteMode()));
        preview.setTeamId(trim(dto.getTeamId()));
        if (Objects.equals(preview.getWriteMode(), LinearWriteMode.COMMENT.getCode())) {
            ProjectToolBinding binding = resolveLinearPrimaryBinding(decisionItem.getProjectId());
            preview.setTargetIssueId(firstNonBlank(trim(dto.getTargetIssueId()), binding == null ? null : binding.getExternalId()));
            preview.setTargetIssueIdentifier(firstNonBlank(trim(dto.getTargetIssueIdentifier()), binding == null ? null : binding.getExternalKey()));
            preview.setTargetIssueUrl(firstNonBlank(trim(dto.getTargetIssueUrl()), binding == null ? null : binding.getExternalUrl()));
            if (preview.getTargetIssueId() == null || preview.getTargetIssueId().isBlank()) {
                throw new BusinessException(400, "当前项目还没有可用于评论的 Linear 主工作项。请先在项目中心建立 Linear 项目映射，或切换为 Issue 模式");
            }
            preview.setBody(firstNonBlank(
                    trim(dto.getBody()),
                    """
                    [AICoOS 决策事项]
                    标题：%s
                    类型：%s
                    描述：%s
                    推荐选项：%s
                    预算影响：%s
                    项目影响：%s
                    状态：%s
                    """.formatted(
                            firstNonBlank(decisionItem.getTitle(), "-"),
                            firstNonBlank(decisionItem.getItemType(), "-"),
                            firstNonBlank(decisionItem.getDescription(), "-"),
                            firstNonBlank(decisionItem.getRecommendedOption(), "-"),
                            firstNonBlank(decisionItem.getBudgetImpactSummary(), "-"),
                            firstNonBlank(decisionItem.getProjectImpactSummary(), "-"),
                            firstNonBlank(decisionItem.getStatus(), "-")
                    ).trim()
            ));
            preview.setOperationSummary("将把决策事项作为 Comment 写入该项目的 Linear 主工作项。");
        } else {
            preview.setTitle(firstNonBlank(
                    trim(dto.getTitle()),
                    "[AICoOS 决策] " + firstNonBlank(decisionItem.getTitle(), decisionItem.getId())
            ));
            preview.setBody(firstNonBlank(
                    trim(dto.getBody()),
                    """
                    ## AICoOS 决策事项

                    - 类型：%s
                    - 描述：%s
                    - 影响摘要：%s
                    - 推荐选项：%s
                    - 预算影响：%s
                    - 项目影响：%s
                    - 阻塞：%s
                    - 当前状态：%s
                    """.formatted(
                            firstNonBlank(decisionItem.getItemType(), "-"),
                            firstNonBlank(decisionItem.getDescription(), "-"),
                            firstNonBlank(decisionItem.getImpactSummary(), "-"),
                            firstNonBlank(decisionItem.getRecommendedOption(), "-"),
                            firstNonBlank(decisionItem.getBudgetImpactSummary(), "-"),
                            firstNonBlank(decisionItem.getProjectImpactSummary(), "-"),
                            Objects.equals(decisionItem.getBlockerFlag(), 1) ? "是" : "否",
                            firstNonBlank(decisionItem.getStatus(), "-")
                    ).trim()
            ));
            preview.setOperationSummary("将把决策事项作为独立 Linear Issue 写入外部交付工具。");
        }
        return preview;
    }

    private ProjectToolBinding resolveLinearPrimaryBinding(String projectId) {
        return projectToolBindingService.getDefaultBinding(
                projectId,
                ExternalToolType.LINEAR.getCode(),
                ProjectToolBindingType.LINEAR_PRIMARY_ISSUE.getCode()
        );
    }

    private String actionTypeForWriteMode(String writeMode, boolean preview) {
        boolean comment = Objects.equals(writeMode, LinearWriteMode.COMMENT.getCode());
        if (preview) {
            return comment
                    ? ToolIntegrationActionType.LINEAR_COMMENT_PREVIEW.getCode()
                    : ToolIntegrationActionType.LINEAR_ISSUE_PREVIEW.getCode();
        }
        return comment
                ? ToolIntegrationActionType.LINEAR_COMMENT_APPLY.getCode()
                : ToolIntegrationActionType.LINEAR_ISSUE_APPLY.getCode();
    }

    private String normalizeWriteMode(String value) {
        return Objects.equals(trim(value), LinearWriteMode.COMMENT.getCode())
                ? LinearWriteMode.COMMENT.getCode()
                : LinearWriteMode.ISSUE.getCode();
    }

    private Project requireProject(String projectId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    private String buildLinearProjectRepresentationTitle(Project project) {
        return "[AICoOS 项目] " + firstNonBlank(project.getProjectName(), project.getProjectCode(), project.getId());
    }

    private String buildLinearProjectRepresentationDescription(Project project) {
        return """
                ## AICoOS 项目映射

                - 项目编码：%s
                - 项目名称：%s
                - 项目类型：%s
                - 当前阶段：%s
                - 风险等级：%s
                - 当前状态：%s
                """.formatted(
                firstNonBlank(project.getProjectCode(), "-"),
                firstNonBlank(project.getProjectName(), "-"),
                firstNonBlank(project.getProjectType(), "-"),
                firstNonBlank(project.getCurrentStageCode(), "-"),
                firstNonBlank(project.getRiskLevel(), "-"),
                firstNonBlank(project.getStatus(), "-")
        ).trim();
    }

    private FigmaParseResult resolveFigmaTarget(FigmaContextPreviewDTO dto) {
        String fileKey = trim(dto.getFileKey());
        String nodeId = trim(dto.getNodeId());
        String figmaUrl = trim(dto.getFigmaUrl());
        if ((fileKey == null || fileKey.isBlank()) && figmaUrl != null && !figmaUrl.isBlank()) {
            try {
                URI uri = new URI(figmaUrl);
                FigmaParseResult parsed = extractFigmaTargetFromUri(uri);
                fileKey = firstNonBlank(fileKey, parsed.fileKey());
                nodeId = firstNonBlank(nodeId, parsed.nodeId());
            } catch (URISyntaxException _error) {
                throw new BusinessException(400, "Figma 链接格式不正确");
            }
        }
        if (fileKey == null || fileKey.isBlank()) {
            throw new BusinessException(400, "请提供 Figma 链接或 File Key");
        }
        if (nodeId != null && !nodeId.isBlank()) {
            nodeId = nodeId.replace("-", ":");
        }
        return new FigmaParseResult(fileKey, nodeId);
    }

    private FigmaParseResult extractFigmaTargetFromUri(URI uri) {
        String host = trim(uri.getHost());
        if (host == null || !host.endsWith("figma.com")) {
            return new FigmaParseResult(null, null);
        }

        String fileKey = null;
        String[] segments = uri.getPath() == null ? new String[0] : uri.getPath().split("/");
        for (int index = 0; index < segments.length - 1; index++) {
            if (Objects.equals(segments[index], "design") || Objects.equals(segments[index], "file")) {
                fileKey = trim(segments[index + 1]);
                break;
            }
        }

        String nodeId = extractQueryParam(uri.getRawQuery(), "node-id");
        return new FigmaParseResult(fileKey, nodeId);
    }

    private String extractQueryParam(String rawQuery, String key) {
        if (rawQuery == null || rawQuery.isBlank() || key == null || key.isBlank()) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (!Objects.equals(parts[0], key)) {
                continue;
            }
            String value = parts.length > 1 ? parts[1] : "";
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
        return null;
    }

    private void recordPreviewAudit(
            String projectId,
            String toolType,
            String actionType,
            String sourceObjectType,
            String sourceObjectId,
            Object requestPayload,
            Object responsePayload) {
        ToolIntegrationAudit audit = new ToolIntegrationAudit();
        audit.setProjectId(projectId);
        audit.setToolType(toolType);
        audit.setActionType(actionType);
        audit.setSourceObjectType(sourceObjectType);
        audit.setSourceObjectId(sourceObjectId);
        audit.setPreviewFlag(1);
        audit.setConfirmedFlag(0);
        audit.setAuditStatus(ToolIntegrationAuditStatus.PREVIEWED.getCode());
        audit.setOperatorUserId(SecurityUtils.getUserId());
        audit.setRequestPayloadJson(writeJson(requestPayload));
        audit.setResponsePayloadJson(writeJson(responsePayload));
        toolIntegrationAuditService.save(audit);
    }

    private ToolIntegrationAudit recordApplyAuditSuccess(
            String projectId,
            String toolType,
            String actionType,
            String sourceObjectType,
            String sourceObjectId,
            String bindingId,
            Object requestPayload,
            String externalObjectId,
            String externalObjectUrl,
            Object responsePayload) {
        ToolIntegrationAudit audit = new ToolIntegrationAudit();
        audit.setProjectId(projectId);
        audit.setToolType(toolType);
        audit.setActionType(actionType);
        audit.setSourceObjectType(sourceObjectType);
        audit.setSourceObjectId(sourceObjectId);
        audit.setBindingId(bindingId);
        audit.setPreviewFlag(0);
        audit.setConfirmedFlag(1);
        audit.setAuditStatus(ToolIntegrationAuditStatus.SUCCEEDED.getCode());
        audit.setOperatorUserId(SecurityUtils.getUserId());
        audit.setExternalObjectId(externalObjectId);
        audit.setExternalObjectUrl(externalObjectUrl);
        audit.setRequestPayloadJson(writeJson(requestPayload));
        audit.setResponsePayloadJson(writeJson(responsePayload));
        toolIntegrationAuditService.save(audit);
        return audit;
    }

    private void recordApplyAuditFailure(
            String projectId,
            String toolType,
            String actionType,
            String sourceObjectType,
            String sourceObjectId,
            Object requestPayload,
            Exception exception) {
        ToolIntegrationAudit audit = new ToolIntegrationAudit();
        audit.setProjectId(projectId);
        audit.setToolType(toolType);
        audit.setActionType(actionType);
        audit.setSourceObjectType(sourceObjectType);
        audit.setSourceObjectId(sourceObjectId);
        audit.setPreviewFlag(0);
        audit.setConfirmedFlag(1);
        audit.setAuditStatus(ToolIntegrationAuditStatus.FAILED.getCode());
        audit.setOperatorUserId(SecurityUtils.getUserId());
        audit.setRequestPayloadJson(writeJson(requestPayload));
        audit.setErrorMessage(rootMessage(exception));
        toolIntegrationAuditService.save(audit);
    }

    private ProjectToolBinding savedBinding(ProjectToolBinding binding) {
        if (Objects.equals(binding.getDefaultFlag(), 1)) {
            return projectToolBindingService.saveOrReplaceDefaultBinding(binding);
        }
        projectToolBindingService.save(binding);
        return binding;
    }

    private String writeJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (Exception _error) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String textOrDefault(String value, String defaultValue) {
        String next = trim(value);
        return next == null || next.isBlank() ? defaultValue : next;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getClass().getSimpleName() : current.getMessage();
    }

    private record FigmaParseResult(String fileKey, String nodeId) {
    }
}
