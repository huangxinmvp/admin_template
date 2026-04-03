package com.hiking.treasure.modules.phase1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.Project;
import com.hiking.treasure.modules.phase1.entity.RequirementIntake;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationCategory;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationSeverity;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.ClarificationStatus;
import com.hiking.treasure.modules.phase1.mapper.ClarificationItemMapper;
import com.hiking.treasure.modules.phase1.mapper.ProjectMapper;
import com.hiking.treasure.modules.phase1.service.ClarificationItemService;
import com.hiking.treasure.modules.phase1.service.ProjectGovernanceLinkageService;
import com.hiking.treasure.modules.phase1.service.RequirementIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClarificationItemServiceImpl extends ServiceImpl<ClarificationItemMapper, ClarificationItem>
        implements ClarificationItemService {

    private static final int MAX_GENERATED_ITEMS = 4;

    private final ProjectMapper projectMapper;
    private final RequirementIntakeService requirementIntakeService;
    private final ProjectGovernanceLinkageService projectGovernanceLinkageService;

    @Override
    public boolean save(ClarificationItem entity) {
        boolean saved = super.save(entity);
        if (saved) {
            projectGovernanceLinkageService.recomputeProject(entity == null ? null : entity.getProjectId());
        }
        return saved;
    }

    @Override
    public boolean updateById(ClarificationItem entity) {
        String projectId = resolveProjectId(entity == null ? null : entity.getId(), entity == null ? null : entity.getProjectId());
        boolean updated = super.updateById(entity);
        if (updated) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return updated;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        String projectId = resolveProjectId(Objects.toString(id, null), null);
        boolean removed = super.removeById(id);
        if (removed) {
            projectGovernanceLinkageService.recomputeProject(projectId);
        }
        return removed;
    }

    @Override
    public boolean removeByIds(java.util.Collection<?> list) {
        List<java.io.Serializable> ids = normalizeIds(list);
        List<String> projectIds = listByIds(ids).stream()
                .map(ClarificationItem::getProjectId)
                .filter(Objects::nonNull)
                .toList();
        boolean removed = super.removeByIds(ids);
        if (removed) {
            projectGovernanceLinkageService.recomputeProjects(projectIds);
        }
        return removed;
    }

    @Override
    public List<ClarificationItem> generateMockItems(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new BusinessException(400, "项目ID不能为空");
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }

        RequirementIntake intake = requirementIntakeService.getByProjectId(projectId);
        Set<String> existingTitles = list(new LambdaQueryWrapper<ClarificationItem>()
                .eq(ClarificationItem::getProjectId, projectId))
                .stream()
                .map(ClarificationItem::getTitle)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        List<ClarificationBlueprint> blueprints = new ArrayList<>();
        if (intake == null || isBlank(intake.getBusinessGoal())) {
            blueprints.add(new ClarificationBlueprint(
                    "明确业务目标",
                    "本项目最核心的业务目标和衡量成功的结果是什么？",
                    ClarificationCategory.BUSINESS_GOAL.getCode(),
                    ClarificationSeverity.BLOCKER.getCode(),
                    "可选：提升转化率 / 降低人工成本 / 缩短交付周期"));
        }
        if (intake == null || isBlank(intake.getFeatureSummary())) {
            blueprints.add(new ClarificationBlueprint(
                    "明确首期交付范围",
                    "第一阶段必须交付的核心功能范围是什么，哪些内容可以后置？",
                    ClarificationCategory.FEATURE_SCOPE.getCode(),
                    ClarificationSeverity.BLOCKER.getCode(),
                    "可选：MVP 核心流程 / 管理后台 / 数据报表"));
        }
        if (intake == null || isBlank(intake.getTimelineExpectation())) {
            blueprints.add(new ClarificationBlueprint(
                    "明确时间预期",
                    "期望在什么时间范围内看到首个可用版本或关键里程碑？",
                    ClarificationCategory.TIMELINE.getCode(),
                    ClarificationSeverity.HIGH.getCode(),
                    "可选：2 周 / 1 个月 / 1 个季度"));
        }
        if (intake == null || isBlank(intake.getBudgetRange())) {
            blueprints.add(new ClarificationBlueprint(
                    "明确预算范围",
                    "希望控制在怎样的预算范围内，是否存在必须遵守的上限？",
                    ClarificationCategory.BUDGET.getCode(),
                    ClarificationSeverity.HIGH.getCode(),
                    "可选：先做估算 / 有固定上限 / 可阶段追加"));
        }
        if (intake == null || isBlank(intake.getTechnicalConstraints())) {
            blueprints.add(new ClarificationBlueprint(
                    "明确技术约束",
                    "是否有既定技术栈、部署环境、数据合规或集成限制？",
                    ClarificationCategory.TECHNICAL_CONSTRAINT.getCode(),
                    ClarificationSeverity.MEDIUM.getCode(),
                    "可选：指定云环境 / 私有化部署 / 指定数据库"));
        }
        if (intake == null || isBlank(intake.getReferenceProducts())) {
            blueprints.add(new ClarificationBlueprint(
                    "补充参考样例",
                    "是否有参考产品、竞品链接或示意页面帮助我们快速对齐预期？",
                    ClarificationCategory.REFERENCE_BENCHMARK.getCode(),
                    ClarificationSeverity.MEDIUM.getCode(),
                    "可选：竞品链接 / 截图文档 / 原型草图"));
        }
        if (blueprints.isEmpty()) {
            blueprints.add(new ClarificationBlueprint(
                    "明确验收标准",
                    "项目上线前最关键的验收标准和不可妥协的质量要求是什么？",
                    ClarificationCategory.ACCEPTANCE.getCode(),
                    ClarificationSeverity.MEDIUM.getCode(),
                    "可选：性能指标 / 核心流程可用 / 关键报表准确"));
            blueprints.add(new ClarificationBlueprint(
                    "梳理外部依赖",
                    "当前是否存在必须提前确认的第三方系统、账号、API 或数据接入依赖？",
                    ClarificationCategory.INTEGRATION.getCode(),
                    ClarificationSeverity.MEDIUM.getCode(),
                    "可选：支付 / 邮件 / 内部系统 / 设计资源"));
        }

        List<ClarificationItem> created = new ArrayList<>();
        for (ClarificationBlueprint blueprint : blueprints) {
            if (created.size() >= MAX_GENERATED_ITEMS || existingTitles.contains(blueprint.title())) {
                continue;
            }
            ClarificationItem item = new ClarificationItem();
            item.setProjectId(projectId);
            item.setTitle(blueprint.title());
            item.setQuestion(blueprint.question());
            item.setCategory(blueprint.category());
            item.setSeverity(blueprint.severity());
            item.setSuggestedOptions(blueprint.suggestedOptions());
            item.setStatus(ClarificationStatus.OPEN.getCode());
            item.setGeneratedFlag(1);
            save(item);
            created.add(item);
        }
        return created;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String resolveProjectId(String id, String fallbackProjectId) {
        if (fallbackProjectId != null && !fallbackProjectId.isBlank()) {
            return fallbackProjectId;
        }
        if (id == null || id.isBlank()) {
            return null;
        }
        return lambdaQuery()
                .select(ClarificationItem::getProjectId)
                .eq(ClarificationItem::getId, id)
                .last("LIMIT 1")
                .oneOpt()
                .map(ClarificationItem::getProjectId)
                .orElse(null);
    }

    private List<java.io.Serializable> normalizeIds(java.util.Collection<?> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> (java.io.Serializable) item)
                .toList();
    }

    private record ClarificationBlueprint(
            String title,
            String question,
            String category,
            String severity,
            String suggestedOptions) {
    }
}
