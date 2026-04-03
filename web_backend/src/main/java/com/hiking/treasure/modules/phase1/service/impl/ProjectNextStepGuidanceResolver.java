package com.hiking.treasure.modules.phase1.service.impl;

import com.hiking.treasure.modules.phase1.domain.vo.ProjectAgentRoleSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectApprovalSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectBudgetSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectDecisionBudgetCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectDecisionSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectGateConditionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectGovernanceSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectNextStepActionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectNextStepGuidanceVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectProductArchitectureCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectRequirementCollaborationSummaryVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProjectRequirementSummaryVO;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.AgentStageParticipationType;
import com.hiking.treasure.modules.phase1.enums.Phase1DomainEnums.BudgetHealthStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ProjectNextStepGuidanceResolver {

    ProjectNextStepGuidanceVO resolve(ProjectNextStepContext context) {
        ProjectNextStepContext safeContext = context == null ? ProjectNextStepContext.empty() : context;
        List<String> currentBlockers = buildCurrentBlockers(safeContext);

        if (clarificationBlockerCount(safeContext) > 0) {
            return buildClarificationGuidance(safeContext, currentBlockers);
        }
        if (decisionBlockerCount(safeContext) > 0) {
            return buildDecisionGuidance(safeContext, currentBlockers);
        }
        if (approvalBlockerCount(safeContext) > 0) {
            return buildApprovalGuidance(safeContext, currentBlockers);
        }
        if (failedGateCount(safeContext) > 0 || blockingGateCount(safeContext) > 0) {
            return buildGateGuidance(safeContext, currentBlockers);
        }
        if (isBudgetStatus(safeContext, BudgetHealthStatus.OVERRUN.getCode())
                || isBudgetStatus(safeContext, BudgetHealthStatus.PENDING.getCode())
                || isBudgetStatus(safeContext, BudgetHealthStatus.WARNING.getCode())
                || isBudgetStatus(safeContext, BudgetHealthStatus.UNPLANNED.getCode())) {
            return buildBudgetGuidance(safeContext, currentBlockers);
        }
        if (missingCriticalRoleCount(safeContext) > 0) {
            return buildRoleGuidance(safeContext, currentBlockers);
        }

        ProjectNextStepGuidanceVO collaborationGuidance = buildCollaborationGuidance(safeContext, currentBlockers);
        if (collaborationGuidance != null) {
            return collaborationGuidance;
        }
        return buildHealthyStageGuidance(safeContext, currentBlockers);
    }

    private ProjectNextStepGuidanceVO buildClarificationGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        int blockerCount = clarificationBlockerCount(context);
        String stageName = stageName(context);
        String requirementHint = hasRequirementGap(context)
                ? "，并同步补齐需求接收中的核心字段"
                : "";
        return guidance(
                "urgent",
                "优先收敛阻塞澄清项并确认需求边界",
                stageName + "阶段仍有 " + blockerCount + " 个阻塞澄清项，继续推进会放大范围、预算与审批不确定性" + requirementHint + "。",
                currentBlockers,
                resolveActor(context, "clarification_manage", "需求分析 / 产品经理"),
                "clarification-center",
                actions(
                        action(
                                "在澄清中心优先关闭阻塞澄清项",
                                "clarification-center",
                                "阻塞澄清项直接卡住需求边界确认，是当前最先需要收敛的治理问题。",
                                "处理需求边界不清、范围漂移和预算误判风险",
                                "项目会先形成可确认的需求边界，后续决策和预算动作会更稳定。"),
                        action(
                                "将影响范围或预算的事项升级为正式决策",
                                "decision-center",
                                "澄清层已经识别出高影响事项，下一步应进入正式确认层。",
                                "处理关键范围和预算影响只停留在问题层的风险",
                                "项目会形成可追踪的正式决策事项。"),
                        hasRequirementGap(context)
                                ? action(
                                "补齐需求接收核心字段",
                                "requirement-intake",
                                "需求接收字段缺口会持续影响澄清质量和后续治理判断。",
                                "处理基础输入不完整的风险",
                                "需求接收信息会更完整，下一轮澄清会更聚焦。")
                                : action(
                                "回收用户回复并更新澄清状态",
                                "clarification-center",
                                "没有明显接收缺口时，应尽快把用户反馈同步回澄清状态。",
                                "处理澄清状态滞后和用户反馈未闭环的风险",
                                "澄清池会更接近可关闭或可升级状态。")
                ));
    }

    private ProjectNextStepGuidanceVO buildDecisionGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        int blockerCount = decisionBlockerCount(context);
        return guidance(
                "urgent",
                "尽快完成阻塞决策确认",
                "当前项目存在 " + blockerCount + " 个阻塞决策事项未完成确认，项目无法稳定进入下一治理动作。",
                currentBlockers,
                resolveActor(context, "decision_create", "产品经理"),
                "decision-center",
                actions(
                        action(
                                "在决策中心逐项确认阻塞决策",
                                "decision-center",
                                "阻塞决策未闭环时，项目无法继续稳定推进后续审批、预算和阶段动作。",
                                "处理方向未定、范围未锁定和关键分歧未收敛风险",
                                "项目会形成明确的决策结论，并给预算和审批提供可执行依据。"),
                        action(
                                "补充涉及成本或范围变化的预算影响说明",
                                "budget-center",
                                "关键决策如果缺少预算影响说明，后续治理判断会持续偏弱。",
                                "处理预算影响不透明和成本确认滞后风险",
                                "预算中心会拥有更完整的影响说明，便于后续确认或增补。"),
                        action(
                                "对高影响决策补发审批",
                                "approval-center",
                                "高影响决策需要进入正式审批层，避免仅停留在决策建议状态。",
                                "处理高风险事项未经授权确认的治理风险",
                                "审批层会对关键决策形成明确结论，项目约束条件更加清晰。")
                ));
    }

    private ProjectNextStepGuidanceVO buildApprovalGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        int blockerCount = approvalBlockerCount(context);
        return guidance(
                "urgent",
                "推动阻塞审批形成明确结论",
                "当前有 " + blockerCount + " 个阻塞审批仍未闭环，相关决策和预算动作不应继续向前推进。",
                currentBlockers,
                resolveActor(context, "approval_request", "审批人 / PMO"),
                "approval-center",
                actions(
                        action(
                                "在审批中心处理阻塞审批记录",
                                "approval-center",
                                "阻塞审批未形成结论时，项目无法安全推进依赖它的后续动作。",
                                "处理高影响事项未获正式确认的治理风险",
                                "审批状态会更明确，关联的决策和预算动作也能继续收敛。"),
                        action(
                                "补充审批背景、风险与预算说明",
                                "approval-center",
                                "审批信息不完整会拖慢判断效率，也容易造成反复沟通。",
                                "处理审批依据不足和风险说明不充分的问题",
                                "审批人能更快形成明确判断，审批闭环效率会提高。"),
                        action(
                                "同步回看受影响的决策事项",
                                "decision-center",
                                "审批结论通常会影响相关决策的状态和后续建议。",
                                "处理审批与决策状态脱节的风险",
                                "决策中心中的相关事项会更贴近最新治理结论。")
                ));
    }

    private ProjectNextStepGuidanceVO buildGateGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        boolean useWorkflowTemplate = context.workflowTemplateId() != null && !context.workflowTemplateId().isBlank();
        String targetModule = useWorkflowTemplate ? "workflow-templates" : "project-center";
        String reason = failedGateCount(context) > 0
                ? "当前阶段存在失败门禁条件，治理要求尚未满足。"
                : "当前阶段存在阻塞门禁条件，关键前置项还没有闭环。";
        return guidance(
                "urgent",
                "先处理当前阶段门禁条件",
                reason,
                currentBlockers,
                resolveActor(context, "stage_suggestion_update", "交付负责人 / PMO"),
                targetModule,
                actions(
                        action(
                                "回看当前门禁条件并确认阻塞原因",
                                "project-center",
                                "门禁条件是当前阶段的显式治理边界，先定位失败或阻塞原因最关键。",
                                "处理阶段前置条件未满足导致的推进风险",
                                "项目会明确知道卡点在哪个治理条件上，后续动作可以更聚焦。"),
                        useWorkflowTemplate
                                ? action(
                                "检查工作流模板中的门禁配置",
                                "workflow-templates",
                                "如果门禁来自模板要求，需要先确认配置是否与当前项目治理意图一致。",
                                "处理模板规则与项目当前状态不匹配的风险",
                                "运营人员会更清楚是执行问题还是模板配置问题。")
                                : action(
                                "补齐当前阶段所需前置项",
                                "project-center",
                                "没有模板调整需求时，优先补齐当前阶段缺失的治理前置项。",
                                "处理阶段前置材料、确认项或治理记录缺失风险",
                                "当前门禁条件会更接近可通过状态。"),
                        action(
                                "同步处理关联决策、审批或预算项",
                                "project-center",
                                "门禁失败往往不是孤立问题，通常还伴随决策、审批或预算缺口。",
                                "处理治理信号之间未联动收敛的风险",
                                "项目中心中的整体治理状态会更加一致。")
                ));
    }

    private ProjectNextStepGuidanceVO buildBudgetGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        String budgetStatus = budgetStatus(context);
        String nextStep;
        String reason;
        String priority;
        if (Objects.equals(budgetStatus, BudgetHealthStatus.OVERRUN.getCode())) {
            nextStep = "立即处理预算超支并确认后续执行边界";
            reason = "预算已进入超支状态，继续推进会直接影响项目可交付性和治理安全。";
            priority = "urgent";
        } else if (Objects.equals(budgetStatus, BudgetHealthStatus.PENDING.getCode())) {
            nextStep = "尽快确认预算基线或预算增补事项";
            reason = "预算当前处于待确认状态，关键投入与角色安排还没有形成可执行基线。";
            priority = "high";
        } else if (Objects.equals(budgetStatus, BudgetHealthStatus.UNPLANNED.getCode())) {
            nextStep = "先建立项目预算基线";
            reason = "项目尚未形成预算规划，预算与角色投入无法被治理视图有效跟踪。";
            priority = "high";
        } else {
            nextStep = "处理预算预警并收敛成本风险";
            reason = "预算已进入预警区间，建议在进一步扩展范围前先明确预算缓冲和确认动作。";
            priority = "high";
        }
        return guidance(
                priority,
                nextStep,
                reason,
                currentBlockers,
                resolveActor(context, "budget_change_propose", "预算分析 / 产品经理"),
                "budget-center",
                actions(
                        action(
                                "在预算中心检查预算健康与最近流水",
                                "budget-center",
                                "预算异常应先定位是超支、待确认还是未规划，再决定后续治理动作。",
                                "处理预算健康不明和成本变化来源不清的风险",
                                "项目会形成更清楚的预算现状判断，方便后续确认或调整。"),
                        action(
                                "必要时补提预算确认或预算变更审批",
                                "approval-center",
                                "预算影响较大的动作应进入正式确认层，避免只在说明层停留。",
                                "处理预算扩大或超支未获正式授权的风险",
                                "关键预算动作会得到明确审批结论，预算边界更稳定。"),
                        action(
                                "同步回看受影响的决策事项",
                                "decision-center",
                                "预算变化通常意味着范围和优先级判断也需要同步复核。",
                                "处理决策结论与预算现实脱节的风险",
                                "相关决策的影响说明会更完整，项目取舍更透明。")
                ));
    }

    private ProjectNextStepGuidanceVO buildRoleGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        List<String> missingRoles = safe(context.missingCriticalRoles());
        String missingRoleSummary = missingRoles.isEmpty()
                ? "当前阶段缺少关键角色参与。"
                : missingRoles.get(0);
        return guidance(
                "high",
                "补齐当前阶段关键角色覆盖",
                missingRoleSummary + " 没有合适的角色参与会削弱澄清、决策、预算和审批建议的可执行性。",
                currentBlockers,
                "PMO / 资源协调",
                "agent-roles",
                actions(
                        action(
                                "在 Agent 角色管理中核对当前阶段必需角色",
                                "agent-roles",
                                "关键角色缺失会直接降低当前阶段治理建议的可信度和可执行性。",
                                "处理关键职责无人承担的治理风险",
                                "当前阶段所需角色覆盖会更完整，后续建议更可执行。"),
                        action(
                                "补齐关键角色后重新回看项目中心建议",
                                "project-center",
                                "角色补齐后，项目中心建议通常会发生变化，适合重新确认优先级。",
                                "处理角色变更后治理建议未同步更新的风险",
                                "下一步建议会更贴近当前真实执行条件。"),
                        action(
                                "确认角色允许动作是否覆盖当前治理需要",
                                "agent-roles",
                                "只有角色存在还不够，还需要确保其允许动作能支撑当前治理任务。",
                                "处理角色配置存在但无法执行关键治理动作的风险",
                                "角色参与和动作权限会更一致。")
                ));
    }

    private ProjectNextStepGuidanceVO buildCollaborationGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        ProjectCollaborationSummaryVO collaborationSummary = context.collaborationSummary();
        if (collaborationSummary == null) {
            return null;
        }

        ProjectRequirementCollaborationSummaryVO requirementCollaboration =
                collaborationSummary.getRequirementClarification();
        if (requirementCollaboration != null
                && zeroIfNull(requirementCollaboration.getDecisionEscalationSuggestionCount()) > 0) {
            int suggestionCount = zeroIfNull(requirementCollaboration.getDecisionEscalationSuggestionCount());
            return guidance(
                    "high",
                    "将关键澄清结果升级为正式决策事项",
                    "多角色澄清评审已经识别出 " + suggestionCount + " 个建议升级的事项，适合转入正式确认层。",
                    currentBlockers,
                    resolveActor(context, "decision_create", "产品经理"),
                    "decision-center",
                    actions(
                            action(
                                    "回看建议升级的澄清项",
                                    "clarification-center",
                                    "协作结果已经指出哪些澄清项具有正式确认价值，适合先做人工复核。",
                                    "处理重要澄清结果仍停留在问题层而未升级治理层的风险",
                                    "需要升级的事项会被识别得更清楚。"),
                            action(
                                    "在决策中心创建并确认正式决策",
                                    "decision-center",
                                    "关键澄清结论进入决策中心后，项目才能形成正式确认记录。",
                                    "处理重要结论未形成正式治理记录的风险",
                                    "项目会拥有明确的确认项和后续执行边界。"),
                            action(
                                    "同步补充预算或审批影响说明",
                                    "budget-center",
                                    "被升级的事项往往会带出预算或审批影响，需要同步补齐说明。",
                                    "处理决策升级后影响面说明缺失的风险",
                                    "预算和审批层会更容易判断是否需要跟进动作。")
                    ));
        }

        ProjectDecisionBudgetCollaborationSummaryVO decisionBudget = collaborationSummary.getDecisionBudget();
        if (decisionBudget != null && zeroIfNull(decisionBudget.getBudgetConfirmationSuggestedCount()) > 0) {
            int suggestedCount = zeroIfNull(decisionBudget.getBudgetConfirmationSuggestedCount());
            return guidance(
                    "high",
                    "复核协作给出的预算确认建议",
                    "已有 " + suggestedCount + " 条决策-预算协作结果建议进一步确认预算影响，适合先做人工复核。",
                    currentBlockers,
                    resolveActor(context, "budget_change_propose", "预算分析 / 产品经理"),
                    "budget-center",
                    actions(
                            action(
                                    "检查最近一次决策预算协作建议",
                                    "project-center",
                                    "协作结果已经给出重点预算提示，先回看建议内容最有效。",
                                    "处理预算影响判断停留在协作结果但未进入治理动作的风险",
                                    "运营人员会更清楚需要确认的是哪类预算变化。"),
                            action(
                                    "在预算中心确认预算缓冲或增补",
                                    "budget-center",
                                    "预算确认需要回到正式预算视图中核对健康状态和变更空间。",
                                    "处理预算边界不清和追加投入未确认的风险",
                                    "预算中心会形成更清晰的确认动作和说明。"),
                            action(
                                    "必要时补发审批",
                                    "approval-center",
                                    "若预算建议超出当前授权边界，应及时进入审批层。",
                                    "处理预算调整未经正式授权的风险",
                                    "预算变化会获得更明确的治理结论。")
                    ));
        }

        ProjectProductArchitectureCollaborationSummaryVO productArchitecture =
                collaborationSummary.getProductArchitecture();
        if (productArchitecture != null
                && (zeroIfNull(productArchitecture.getLatestOpenQuestionCount()) > 0
                || zeroIfNull(productArchitecture.getLatestDecisionCandidateCount()) > 0)) {
            return guidance(
                    "normal",
                    "根据产品与架构简报整理下一轮治理动作",
                    "最近的产品与架构协作仍保留开放问题或决策候选，适合先由运营人员整理后续动作。",
                    currentBlockers,
                    resolveStageLead(context, "项目负责人 / 平台操作人"),
                    "project-center",
                    actions(
                            action(
                                    "回看产品与架构简报中的开放问题",
                                    "project-center",
                                    "简报中的开放问题通常是后续治理动作的最佳起点。",
                                    "处理方案方向未完全收敛的风险",
                                    "项目会明确下一轮应先确认哪些方案问题。"),
                            action(
                                    "将需要确认的事项转入决策中心",
                                    "decision-center",
                                    "方案级候选事项应尽快进入正式决策层，而不是长期停留在简报里。",
                                    "处理方案关键点未形成正式确认记录的风险",
                                    "决策中心会承接真正需要治理闭环的事项。"),
                            action(
                                    "把需要进一步澄清的问题回流到澄清中心",
                                    "clarification-center",
                                    "若问题仍缺少输入，应先回到澄清层补齐事实和边界。",
                                    "处理带着未澄清假设继续做方案判断的风险",
                                    "后续产品和架构建议会建立在更完整的前提上。")
                    ));
        }

        return null;
    }

    private ProjectNextStepGuidanceVO buildHealthyStageGuidance(ProjectNextStepContext context, List<String> currentBlockers) {
        String stageCode = firstNonBlank(context.currentStageCode(), governanceStageCode(context));
        String stageName = stageName(context);
        if (Objects.equals(stageCode, "intake")) {
            return guidance(
                    "normal",
                    hasRequirementGap(context) ? "补齐需求接收核心信息" : "完成需求接收并准备澄清推进",
                    hasRequirementGap(context)
                            ? "当前项目仍处于需求接收阶段，建议先补齐核心字段后再继续推进澄清。"
                            : "需求接收已基本成形，下一步适合把要点转入澄清与确认。",
                    currentBlockers,
                    resolveActor(context, "clarification_manage", "需求分析 / 产品经理"),
                    hasRequirementGap(context) ? "requirement-intake" : "clarification-center",
                    actions(
                            action(
                                    "回看业务目标、功能摘要和预算范围",
                                    "requirement-intake",
                                    "需求接收阶段最先要确认的是项目目标、范围和预算边界是否基本完整。",
                                    "处理需求基础信息不完整的风险",
                                    "项目会形成更稳定的接收基线。"),
                            action(
                                    "生成或整理下一轮澄清问题",
                                    "clarification-center",
                                    "当接收信息基本成形后，下一步就应该把开放问题转入澄清层。",
                                    "处理接收信息停留在摘要而未转入治理问题的风险",
                                    "澄清中心会接住下一轮需要用户确认的问题。"),
                            action(
                                    "同步确认参考产品与技术约束",
                                    "requirement-intake",
                                    "这类背景信息会直接影响后续方案判断和预算估算。",
                                    "处理需求输入缺少外部参考和技术边界的风险",
                                    "后续澄清和方案建议会更贴近真实约束。")
                    ));
        }
        if (Objects.equals(stageCode, "clarification")) {
            return guidance(
                    "normal",
                    "收敛澄清结论并识别需要正式确认的事项",
                    stageName + "阶段当前没有硬阻塞，适合把开放问题收敛为可执行的确认项与决策项。",
                    currentBlockers,
                    resolveActor(context, "clarification_manage", "需求分析 / 产品经理"),
                    "clarification-center",
                    actions(
                            action(
                                    "关闭已解决澄清并标记仍待用户回复事项",
                                    "clarification-center",
                                    "没有硬阻塞时，最先要做的是把澄清池收敛成清晰的待办与已解决状态。",
                                    "处理澄清状态混杂、难以区分已解决与待确认的问题",
                                    "澄清中心会更清楚地暴露剩余待确认项。"),
                            action(
                                    "将关键确认项升级到决策中心",
                                    "decision-center",
                                    "澄清阶段收敛出的关键选择题适合尽快进入正式确认层。",
                                    "处理重要确认事项长期停留在问题层的风险",
                                    "决策中心会承接更明确的正式确认事项。"),
                            action(
                                    "必要时更新需求接收摘要",
                                    "requirement-intake",
                                    "当澄清已经明显改变项目理解时，接收摘要也应同步更新。",
                                    "处理最新需求理解未同步回写的问题",
                                    "项目基础信息会与最新澄清结论保持一致。")
                    ));
        }
        if (Objects.equals(stageCode, "estimation")) {
            return guidance(
                    "normal",
                    "确认预算基线与角色投入假设",
                    "项目已进入预算估算阶段，适合把当前范围和角色配置收敛为可跟踪的预算基线。",
                    currentBlockers,
                    resolveActor(context, "budget_change_propose", "预算分析 / 产品经理"),
                    "budget-center",
                    actions(
                            action(
                                    "核对预算基线、锁定金额和待增补金额",
                                    "budget-center",
                                    "估算阶段最关键的是把预算从粗略想法收敛成可跟踪基线。",
                                    "处理预算边界模糊和投入口径不统一的风险",
                                    "项目会拥有更可执行的预算基线。"),
                            action(
                                    "同步确认关键角色预算系数",
                                    "agent-roles",
                                    "角色投入假设会直接影响预算可靠性，需要同步核对。",
                                    "处理预算模型与角色配置脱节的风险",
                                    "预算假设会与角色组织层保持一致。"),
                            action(
                                    "把重大预算差异回写到决策说明",
                                    "decision-center",
                                    "重要预算变化应反映到决策层，帮助运营人员做取舍。",
                                    "处理预算变化未进入正式确认说明的风险",
                                    "后续决策会更清楚地体现成本影响。")
                    ));
        }
        if (Objects.equals(stageCode, "approval") || Objects.equals(stageCode, "release_approval")) {
            return guidance(
                    "normal",
                    "推动当前阶段审批形成明确结论",
                    stageName + "阶段适合优先完成审批闭环，保证后续动作具备治理依据。",
                    currentBlockers,
                    resolveActor(context, "approval_request", "审批人 / PMO"),
                    "approval-center",
                    actions(
                            action(
                                    "回看当前阶段相关审批项",
                                    "approval-center",
                                    "审批阶段的首要任务是核对哪些审批仍未形成清晰结论。",
                                    "处理审批池状态不清导致的推进迟滞",
                                    "项目会更明确知道当前还差哪些审批闭环。"),
                            action(
                                    "补充风险与预算背景说明",
                                    "approval-center",
                                    "审批结论质量很依赖背景材料的完整性。",
                                    "处理审批依据不足和风险说明不充分的问题",
                                    "审批判断会更顺畅，反复补材料会减少。"),
                            action(
                                    "同步更新项目中心治理摘要",
                                    "project-center",
                                    "审批推进后，项目中心需要反映最新治理状态和关注点。",
                                    "处理审批进展与项目治理视图不同步的问题",
                                    "项目中心会更准确地显示当前建议和风险。")
                    ));
        }
        return guidance(
                "normal",
                "继续推进当前阶段的治理闭环",
                stageName + "阶段当前没有明显硬阻塞，建议围绕阶段目标收敛开放问题、预算说明和角色参与。",
                currentBlockers,
                resolveStageLead(context, "交付负责人 / PMO"),
                "project-center",
                actions(
                        action(
                                "回看当前阶段目标与最近活动",
                                "project-center",
                                "没有硬阻塞时，应先确认当前阶段目标与最近治理动作是否一致。",
                                "处理项目推进方向分散、关注点不聚焦的风险",
                                "项目中心会更清楚地呈现当前阶段真正的推进重点。"),
                        action(
                                "同步处理与阶段相关的决策或审批事项",
                                "decision-center",
                                "阶段推进通常还需要同步清理未闭环的决策或审批动作。",
                                "处理阶段推进与治理闭环节奏脱节的风险",
                                "相关治理事项会更贴近当前阶段目标。"),
                        action(
                                "确认关键角色参与和预算影响",
                                "agent-roles",
                                "阶段推进前确认角色与预算影响，可以降低后续返工。",
                                "处理执行角色准备不足和预算影响被低估的风险",
                                "项目会以更稳定的资源和预算假设继续推进。")
                ));
    }

    private ProjectNextStepGuidanceVO guidance(
            String priority,
            String nextStep,
            String reason,
            List<String> currentBlockers,
            String actorRole,
            String targetModule,
            List<ProjectNextStepActionVO> actions) {
        ProjectNextStepGuidanceVO guidance = new ProjectNextStepGuidanceVO();
        guidance.setRecommendedPriority(priority);
        guidance.setRecommendedNextStep(nextStep);
        guidance.setRecommendedReason(reason);
        guidance.setCurrentBlockers(currentBlockers);
        guidance.setRecommendedActorRole(actorRole);
        guidance.setRecommendedTargetModule(targetModule);
        guidance.setPriorityActions(actions);
        return guidance;
    }

    private List<String> buildCurrentBlockers(ProjectNextStepContext context) {
        List<String> blockers = new ArrayList<>();
        if (clarificationBlockerCount(context) > 0) {
            blockers.add("阻塞澄清项 " + clarificationBlockerCount(context));
        }
        if (decisionBlockerCount(context) > 0) {
            blockers.add("阻塞决策 " + decisionBlockerCount(context));
        }
        if (approvalBlockerCount(context) > 0) {
            blockers.add("阻塞审批 " + approvalBlockerCount(context));
        }
        if (failedGateCount(context) > 0) {
            blockers.add("失败门禁条件 " + failedGateCount(context));
        }
        if (blockingGateCount(context) > 0 && failedGateCount(context) == 0) {
            blockers.add("阻塞门禁条件 " + blockingGateCount(context));
        }
        if (missingCriticalRoleCount(context) > 0) {
            blockers.add("缺失关键角色 " + missingCriticalRoleCount(context));
        }
        if (isBudgetStatus(context, BudgetHealthStatus.OVERRUN.getCode())) {
            blockers.add("预算超支");
        }
        return blockers;
    }

    private String resolveActor(ProjectNextStepContext context, String actionCode, String fallback) {
        List<ProjectAgentRoleSummaryVO> roles = safe(context.currentStageRecommendedRoles());
        ProjectAgentRoleSummaryVO matched = roles.stream()
                .filter(role -> safe(role.getAllowedActionCodes()).contains(actionCode))
                .sorted((left, right) -> Integer.compare(
                        participationPriority(left.getCurrentStageParticipationType()),
                        participationPriority(right.getCurrentStageParticipationType())))
                .findFirst()
                .orElse(null);
        if (matched != null && matched.getRoleName() != null && !matched.getRoleName().isBlank()) {
            return matched.getRoleName();
        }
        return resolveStageLead(context, fallback);
    }

    private String resolveStageLead(ProjectNextStepContext context, String fallback) {
        return safe(context.currentStageRecommendedRoles()).stream()
                .sorted((left, right) -> Integer.compare(
                        participationPriority(left.getCurrentStageParticipationType()),
                        participationPriority(right.getCurrentStageParticipationType())))
                .map(ProjectAgentRoleSummaryVO::getRoleName)
                .filter(Objects::nonNull)
                .filter(roleName -> !roleName.isBlank())
                .findFirst()
                .orElse(fallback);
    }

    private int participationPriority(String participationType) {
        if (Objects.equals(participationType, AgentStageParticipationType.REQUIRED.getCode())) {
            return 0;
        }
        if (Objects.equals(participationType, AgentStageParticipationType.OPTIONAL.getCode())) {
            return 1;
        }
        return 2;
    }

    private boolean hasRequirementGap(ProjectNextStepContext context) {
        ProjectRequirementSummaryVO requirementSummary = context.requirementSummary();
        return requirementSummary != null
                && ((Boolean.FALSE.equals(requirementSummary.getHasRequirementIntake()))
                || zeroIfNull(requirementSummary.getCompletenessScore()) < 80);
    }

    private int clarificationBlockerCount(ProjectNextStepContext context) {
        Integer fromGovernance = context.governanceSummary() == null
                ? null
                : context.governanceSummary().getBlockerClarificationCount();
        Integer fromRequirement = context.requirementSummary() == null
                ? null
                : context.requirementSummary().getBlockerCount();
        return zeroIfNull(firstNonNull(fromGovernance, fromRequirement));
    }

    private int decisionBlockerCount(ProjectNextStepContext context) {
        Integer fromGovernance = context.governanceSummary() == null
                ? null
                : context.governanceSummary().getBlockerDecisionCount();
        Integer fromDecision = context.decisionSummary() == null
                ? null
                : context.decisionSummary().getBlockerCount();
        return zeroIfNull(firstNonNull(fromGovernance, fromDecision));
    }

    private int approvalBlockerCount(ProjectNextStepContext context) {
        Integer fromGovernance = context.governanceSummary() == null
                ? null
                : context.governanceSummary().getBlockerApprovalCount();
        Integer fromApproval = context.approvalSummary() == null
                ? null
                : context.approvalSummary().getBlockerCount();
        return zeroIfNull(firstNonNull(fromGovernance, fromApproval));
    }

    private int failedGateCount(ProjectNextStepContext context) {
        Integer fromGovernance = context.governanceSummary() == null
                ? null
                : context.governanceSummary().getFailedGateConditionCount();
        if (fromGovernance != null) {
            return zeroIfNull(fromGovernance);
        }
        return (int) safe(context.gateConditions()).stream()
                .filter(item -> Objects.equals(item.getStatus(), "blocked"))
                .count();
    }

    private int blockingGateCount(ProjectNextStepContext context) {
        Integer fromGovernance = context.governanceSummary() == null
                ? null
                : context.governanceSummary().getBlockingGateConditionCount();
        if (fromGovernance != null) {
            return zeroIfNull(fromGovernance);
        }
        return (int) safe(context.gateConditions()).stream()
                .filter(item -> Objects.equals(item.getStatus(), "blocked")
                        || Objects.equals(item.getStatus(), "warning"))
                .count();
    }

    private int missingCriticalRoleCount(ProjectNextStepContext context) {
        Integer fromGovernance = context.governanceSummary() == null
                ? null
                : context.governanceSummary().getMissingCriticalRoleCount();
        if (fromGovernance != null) {
            return zeroIfNull(fromGovernance);
        }
        return safe(context.missingCriticalRoles()).size();
    }

    private boolean isBudgetStatus(ProjectNextStepContext context, String expected) {
        return Objects.equals(budgetStatus(context), expected);
    }

    private String budgetStatus(ProjectNextStepContext context) {
        return firstNonBlank(
                context.governanceSummary() == null ? null : context.governanceSummary().getBudgetStatus(),
                context.budgetSummary() == null ? null : context.budgetSummary().getStatus());
    }

    private String stageName(ProjectNextStepContext context) {
        return firstNonBlank(
                context.currentStageName(),
                context.governanceSummary() == null ? null : context.governanceSummary().getCurrentStageName(),
                "当前阶段");
    }

    private String governanceStageCode(ProjectNextStepContext context) {
        return context.governanceSummary() == null ? null : context.governanceSummary().getCurrentStageCode();
    }

    private List<ProjectNextStepActionVO> actions(ProjectNextStepActionVO... actions) {
        List<ProjectNextStepActionVO> items = new ArrayList<>();
        if (actions == null) {
            return items;
        }
        for (ProjectNextStepActionVO action : actions) {
            if (action != null && action.getActionLabel() != null && !action.getActionLabel().isBlank()) {
                items.add(action);
            }
        }
        List<ProjectNextStepActionVO> limited = items.stream().limit(3).toList();
        for (int index = 0; index < limited.size(); index++) {
            limited.get(index).setActionOrder(index + 1);
        }
        return limited;
    }

    private ProjectNextStepActionVO action(
            String actionLabel,
            String targetModule,
            String recommendedReason,
            String addressedRisk,
            String expectedChange) {
        ProjectNextStepActionVO action = new ProjectNextStepActionVO();
        action.setActionLabel(actionLabel);
        action.setTargetModule(targetModule);
        action.setRecommendedReason(recommendedReason);
        action.setAddressedRisk(addressedRisk);
        action.setExpectedChange(expectedChange);
        return action;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
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

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private <T> List<T> safe(List<T> items) {
        return items == null ? List.of() : items;
    }

    record ProjectNextStepContext(
            String currentStageCode,
            String currentStageName,
            String workflowTemplateId,
            ProjectGovernanceSummaryVO governanceSummary,
            ProjectRequirementSummaryVO requirementSummary,
            ProjectDecisionSummaryVO decisionSummary,
            ProjectApprovalSummaryVO approvalSummary,
            ProjectBudgetSummaryVO budgetSummary,
            ProjectCollaborationSummaryVO collaborationSummary,
            List<ProjectGateConditionVO> gateConditions,
            List<String> missingCriticalRoles,
            List<ProjectAgentRoleSummaryVO> currentStageRecommendedRoles) {

        static ProjectNextStepContext empty() {
            return new ProjectNextStepContext(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of()
            );
        }
    }
}
