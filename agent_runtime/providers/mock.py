from __future__ import annotations

from itertools import islice

from agent_runtime.schemas import (
    BudgetImpactSuggestion,
    BudgetImpactSuggestionRequest,
    BudgetImpactSuggestionResponse,
    ClarificationGenerationRequest,
    ClarificationGenerationResponse,
    ClarificationSuggestion,
    DecisionBudgetReviewRequest,
    DecisionBudgetReviewResponse,
    DecisionBudgetReviewSuggestion,
    DecisionPromotionSuggestion,
    DecisionPromotionSuggestionRequest,
    DecisionPromotionSuggestionResponse,
    MeetingDecisionCandidate,
    MeetingSummaryRequest,
    MeetingSummaryResponse,
    MeetingSummarySuggestion,
    ProductArchitectureBriefRequest,
    ProductArchitectureBriefResponse,
    ProductArchitectureBriefSuggestion,
    RequirementClarificationReviewRequest,
    RequirementClarificationReviewResponse,
)


class MockSuggestionProvider:
    def __init__(self, model_name: str = "mock-suggestion-v1") -> None:
        self._model_name = model_name

    @property
    def provider_name(self) -> str:
        return "mock"

    @property
    def model_name(self) -> str:
        return self._model_name

    def generate_clarifications(
        self,
        payload: ClarificationGenerationRequest,
    ) -> ClarificationGenerationResponse:
        suggestions: list[ClarificationSuggestion] = []
        if not self._filled(payload.business_goal):
            suggestions.append(
                ClarificationSuggestion(
                    title="明确业务目标边界",
                    question="这个项目最重要的业务结果是什么，优先解决哪个核心痛点？",
                    category="business_goal",
                    severity="blocker",
                    suggestedOptions="提升转化率 / 降低人工成本 / 缩短交付周期",
                    blockerFlag=True,
                    reason="业务目标缺失会直接影响需求取舍、范围控制和后续预算估算。",
                    governanceReason="先补齐业务目标输入，再进入更细的范围和预算判断。",
                    followUpModule="clarification_center",
                    escalationRecommended=False,
                )
            )
        if not self._filled(payload.feature_summary):
            suggestions.append(
                ClarificationSuggestion(
                    title="拆分首期功能范围",
                    question="首期上线必须交付哪些核心功能，哪些内容可以延后到下一阶段？",
                    category="feature_scope",
                    severity="blocker",
                    suggestedOptions="先做 MVP 主链路 / 同步做后台 / 报表与高级能力后置",
                    blockerFlag=True,
                    reason="功能范围不清晰会让估算、排期和方案评审都处于漂移状态。",
                    governanceReason="范围边界会直接影响 MVP 承诺，必要时应人工升级到决策中心。",
                    followUpModule="decision_center",
                    escalationRecommended=True,
                )
            )
        if not self._filled(payload.timeline_expectation):
            suggestions.append(
                ClarificationSuggestion(
                    title="确认里程碑节奏",
                    question="预期在什么时候看到第一个可演示版本，是否有强制上线节点？",
                    category="timeline",
                    severity="high",
                    suggestedOptions="2 周出原型 / 4 周出 MVP / 8-12 周完整一期",
                    blockerFlag=False,
                    reason="时间预期会影响方案复杂度和人员投入结构。",
                    governanceReason="时间窗口会约束方案复杂度和资源投入，建议形成显式确认。",
                    followUpModule="decision_center",
                    escalationRecommended=True,
                )
            )
        if not self._filled(payload.budget_range):
            suggestions.append(
                ClarificationSuggestion(
                    title="确认预算上限",
                    question="当前是否有预算上限、阶段预算分配，或者需要先做估算再定预算？",
                    category="budget",
                    severity="high",
                    suggestedOptions="先做粗估 / 有明确上限 / 可分阶段追加",
                    blockerFlag=False,
                    reason="预算范围缺失时，后续的方案和角色投入建议缺少约束条件。",
                    governanceReason="预算边界会影响资源承诺，建议尽早进入正式确认链路。",
                    followUpModule="decision_center",
                    escalationRecommended=True,
                )
            )
        if not self._filled(payload.technical_constraints):
            suggestions.append(
                ClarificationSuggestion(
                    title="补充技术约束",
                    question="是否有必须遵守的技术栈、部署方式、合规要求或系统集成边界？",
                    category="technical_constraint",
                    severity="medium",
                    suggestedOptions="私有化部署 / 指定云环境 / 必须接现有系统 / 合规限制",
                    blockerFlag=False,
                    reason="技术约束不明确会导致架构方向和交付成本偏差。",
                    governanceReason="约束先在澄清中心补齐，必要时再进入方案决策。",
                    followUpModule="clarification_center",
                    escalationRecommended=False,
                )
            )
        if not suggestions:
            suggestions.append(
                ClarificationSuggestion(
                    title="确认验收标准",
                    question="在用户可接受交付时，哪些结果必须满足才算阶段成功？",
                    category="acceptance",
                    severity="medium",
                    suggestedOptions="核心流程可用 / 数据正确 / 性能达标 / 培训可落地",
                    blockerFlag=False,
                    reason="即便基础信息完整，也需要提前锁定验收标准来减少返工。",
                    governanceReason="验收标准适合作为澄清留档，暂不需要单独升级决策。",
                    followUpModule="clarification_center",
                    escalationRecommended=False,
                )
            )
        return ClarificationGenerationResponse(
            provider=self.provider_name,
            model=self.model_name,
            suggestions=list(islice(suggestions, 5)),
        )

    def suggest_decision_promotions(
        self,
        payload: DecisionPromotionSuggestionRequest,
    ) -> DecisionPromotionSuggestionResponse:
        suggestions: list[DecisionPromotionSuggestion] = []
        for clarification in payload.clarifications:
            should_promote = (
                clarification.severity in {"blocker", "high"}
                or clarification.category in {"budget", "timeline", "feature_scope", "technical_constraint"}
            )
            if not should_promote:
                continue
            decision_type = {
                "budget": "budget_change",
                "timeline": "timeline_confirmation",
                "feature_scope": "scope_change",
            }.get(clarification.category or "", "clarification")
            suggestions.append(
                DecisionPromotionSuggestion(
                    clarificationId=clarification.clarification_id,
                    suggestedTitle=clarification.title or "待确认事项",
                    type=decision_type,
                    impactSummary="该事项会影响项目范围、排期、预算或方案稳定性，建议升级为正式决策。",
                    suggestedOptions=clarification.suggested_options or "在项目群中补充确认选项",
                    recommendedOption="优先采用业务负责人明确确认的首选方案",
                    blockerFlag=clarification.severity == "blocker",
                    reason="当前澄清项具有治理影响，适合进入正式决策链路。",
                )
            )
        if not suggestions and payload.clarifications:
            first = payload.clarifications[0]
            suggestions.append(
                DecisionPromotionSuggestion(
                    clarificationId=first.clarification_id,
                    suggestedTitle=first.title or "建议建立显式确认项",
                    type="clarification",
                    impactSummary="当前虽然不是明显阻塞项，但建议建立显式确认记录来沉淀结论。",
                    suggestedOptions=first.suggested_options or "待补充建议选项",
                    recommendedOption="保持建议项观察，如用户回复后再正式提升",
                    blockerFlag=False,
                    reason="该项目当前缺少显式确认闭环，可先建立一条低风险决策记录。",
                )
            )
        return DecisionPromotionSuggestionResponse(
            provider=self.provider_name,
            model=self.model_name,
            suggestions=list(islice(suggestions, 6)),
        )

    def suggest_budget_impact(
        self,
        payload: BudgetImpactSuggestionRequest,
    ) -> BudgetImpactSuggestionResponse:
        decision_type = payload.decision_type or "clarification"
        role_map = {
            "scope_change": ["product_analysis", "frontend", "backend", "qa"],
            "budget_change": ["project_coordination", "product_analysis", "backend"],
            "timeline_confirmation": ["project_coordination", "frontend", "backend"],
            "solution_direction": ["architect", "frontend", "backend"],
            "release": ["qa", "devops", "project_coordination"],
        }
        delta_map = {
            "scope_change": "+15% ~ +30%",
            "budget_change": "+10% ~ +20%",
            "timeline_confirmation": "+5% ~ +12%",
            "solution_direction": "+8% ~ +18%",
            "release": "+3% ~ +8%",
        }
        affected_roles = role_map.get(decision_type, ["product_analysis", "backend"])
        delta_range = delta_map.get(decision_type, "+5% ~ +15%")
        budget_summary = (
            f"基于当前事项，建议预留 {delta_range} 的额外 Token 劳动预算，重点覆盖"
            f"{' / '.join(affected_roles)} 的投入波动。"
        )
        project_summary = (
            "若不提前预留预算缓冲，后续阶段可能在排期、质量或范围上被动压缩。"
        )
        confidence = "medium" if payload.budget_range else "low"
        return BudgetImpactSuggestionResponse(
            provider=self.provider_name,
            model=self.model_name,
            suggestion=BudgetImpactSuggestion(
                budgetImpactSummary=budget_summary,
                projectImpactSummary=project_summary,
                deltaRange=delta_range,
                affectedRoles=affected_roles,
                confidence=confidence,
            ),
        )

    def review_requirement_clarifications(
        self,
        payload: RequirementClarificationReviewRequest,
    ) -> RequirementClarificationReviewResponse:
        clarification_response = self.generate_clarifications(
            ClarificationGenerationRequest(
                projectName=payload.project_name,
                projectType=payload.project_type,
                businessGoal=payload.business_goal,
                featureSummary=payload.feature_summary,
                referenceProducts=payload.reference_products,
                timelineExpectation=payload.timeline_expectation,
                budgetRange=payload.budget_range,
                technicalConstraints=payload.technical_constraints,
                notes=payload.notes,
                meta=payload.meta,
            )
        )
        suggestions = clarification_response.suggestions
        blocker_count = sum(1 for item in suggestions if item.blocker_flag)
        escalation_count = sum(1 for item in suggestions if item.escalation_recommended)
        follow_up_modules = unique_texts(
            item.follow_up_module for item in suggestions if item.follow_up_module
        )
        blocker_assessment = (
            f"当前仍有 {blocker_count} 条阻塞级问题，先补业务目标与首期范围，再进入更细估算。"
            if blocker_count
            else "当前信息已可继续推进，但仍建议把会影响承诺边界的问题先显式留档。"
        )
        next_questions = [item.question for item in islice(suggestions, 3)]
        if not next_questions:
            next_questions = [
                "当前首期交付范围是否已经被业务方明确确认？",
                "预算和时间窗口是否足以支撑当前优先级排序？",
            ]
        recommended_operator_action = build_requirement_operator_action(
            blocker_count=blocker_count,
            escalation_count=escalation_count,
            follow_up_modules=follow_up_modules,
        )
        return RequirementClarificationReviewResponse(
            provider=self.provider_name,
            model=self.model_name,
            participants=["Requirement Analyst", "Product Manager"],
            scenarioLabel="需求澄清协作评审",
            collaborationSummary="先识别阻塞澄清，再区分哪些问题留在澄清中心、哪些需要人工升级为正式确认。",
            recommendedOperatorAction=recommended_operator_action,
            governanceLinkage={
                "targetModule": "clarification_center",
                "currentStageCode": payload.current_stage_code or "clarification",
                "existingClarificationCount": payload.existing_clarification_count or 0,
                "blockerSuggestionCount": blocker_count,
                "decisionEscalationSuggestionCount": escalation_count,
                "followUpModules": follow_up_modules,
            },
            roleInsights=[
                "Requirement Analyst：先补缺失输入，避免范围、验收和估算失真。",
                "Product Manager：优先锁会改变 MVP、预算或里程碑承诺的问题。",
            ],
            selectionGuidance="先应用阻塞项，再处理会影响 MVP、预算或时间窗口承诺的问题；一般讨论项可保留下一轮确认。",
            governanceInterpretation="澄清中心负责补输入；只有会改变范围、预算或时间承诺的事项才建议人工升级到决策中心。",
            decisionEscalationAdvised=escalation_count > 0,
            followUpHints=build_requirement_follow_up_hints(
                blocker_count=blocker_count,
                escalation_count=escalation_count,
                follow_up_modules=follow_up_modules,
            ),
            blockerAssessment=blocker_assessment,
            nextQuestions=next_questions,
            suggestions=suggestions,
        )

    def review_decision_budget(
        self,
        payload: DecisionBudgetReviewRequest,
    ) -> DecisionBudgetReviewResponse:
        budget_response = self.suggest_budget_impact(
            BudgetImpactSuggestionRequest(
                projectName=payload.project_name,
                projectType=payload.project_type,
                currentStageCode=payload.current_stage_code,
                budgetRange=payload.budget_range,
                decisionTitle=payload.decision_title,
                decisionType=payload.decision_type,
                decisionDescription=payload.decision_description,
                decisionImpactSummary=payload.decision_impact_summary,
                requirementSummary=payload.requirement_summary,
                meta=payload.meta,
            )
        )
        decision_recommendation = (
            "Product Manager：先锁业务收益和首期边界，再决定是否立即推进。"
            if payload.blocker_flag
            else "Product Manager：把推荐选项写入当前决策单，避免继续口头推进。"
        )
        blocker_assessment = (
            "Budget Analyst：若预算缓冲和角色投入未确认，后续交付风险会被放大。"
            if payload.blocker_flag or not self._filled(payload.budget_range)
            else "Budget Analyst：预算影响可控，但仍应留下显式预算说明。"
        )
        suggestion = budget_response.suggestion
        budget_confirmation_advised = payload.blocker_flag or not self._filled(payload.budget_range)
        return DecisionBudgetReviewResponse(
            provider=self.provider_name,
            model=self.model_name,
            participants=["Product Manager", "Budget Analyst"],
            scenarioLabel="决策与预算协作评审",
            collaborationSummary="本次协作重点判断三件事：当前决策是否能直接落档、是否要补预算确认、是否值得继续走人工审批。",
            recommendedOperatorAction=build_decision_budget_operator_action(budget_confirmation_advised),
            governanceLinkage={
                "targetModule": "decision_center",
                "currentStageCode": payload.current_stage_code or "unknown",
                "decisionId": payload.decision_title or "",
                "requiresBudgetConfirmation": budget_confirmation_advised,
                "shouldOpenApproval": payload.blocker_flag or decision_type_requires_manual_approval(payload.decision_type),
            },
            roleInsights={
                "productManagerView": decision_recommendation,
                "budgetAnalystView": blocker_assessment,
            },
            riskFlags=build_decision_risk_flags(payload),
            decisionLinkageSummary=build_decision_linkage_summary(payload),
            budgetLinkageSummary=build_budget_linkage_summary(
                payload=payload,
                budget_confirmation_advised=budget_confirmation_advised,
            ),
            followUpHints=build_decision_follow_up_hints(
                budget_confirmation_advised=budget_confirmation_advised,
                blocker_flag=bool(payload.blocker_flag),
            ),
            suggestion=DecisionBudgetReviewSuggestion(
                decisionRecommendation=decision_recommendation,
                budgetImpactNote=suggestion.budget_impact_summary,
                budgetConfirmationAdvised=budget_confirmation_advised,
                recommendedOption=payload.recommended_option or "优先采用业务收益更明确且预算波动更小的选项",
                projectImpactNote=suggestion.project_impact_summary,
                blockerAssessment=blocker_assessment,
                nextSteps=[
                    "把推荐选项和阻塞判断写入当前决策单。",
                    "补齐预算影响与项目影响说明。",
                    "如仍需预算确认，再人工判断是否进入审批中心。",
                ],
            ),
        )

    def generate_product_architecture_brief(
        self,
        payload: ProductArchitectureBriefRequest,
    ) -> ProductArchitectureBriefResponse:
        project_name = payload.project_name or "当前项目"
        solution_brief = (
            f"Product Manager 与 Architect 建议围绕“{payload.feature_summary or payload.requirement_summary or '核心业务链路'}”优先构建首期可演示闭环，"
            "先锁核心角色、关键集成边界和治理确认点，再扩展增强能力。"
        )
        risks: list[str] = []
        if not self._filled(payload.technical_constraints):
            risks.append("技术约束尚未明确，可能导致方案选型与部署方式反复调整。")
        if not self._filled(payload.business_goal):
            risks.append("业务目标尚未完全锁定，可能造成范围扩张和优先级漂移。")
        if not risks:
            risks.append("当前主要风险集中在范围控制和跨角色协作节奏，需要通过阶段性评审持续校准。")
        open_questions: list[str] = []
        if not self._filled(payload.feature_summary):
            open_questions.append("首期必须交付的 MVP 功能链路是否已经明确？")
        if not self._filled(payload.technical_constraints):
            open_questions.append("是否存在必须遵守的部署、集成或合规约束？")
        if not open_questions:
            open_questions.append("当前推荐方案是否需要拆分为 MVP 与后续迭代两层范围？")
        next_steps = [
            "保存简报，作为后续方案评审和治理留档基线。",
            "把最高风险或最不明确的问题转回澄清/决策中心。",
            "在进入详细设计前先锁定边界与关键约束。",
        ]
        if self._filled(payload.focus_notes):
            next_steps.append("按操作员关注点补充下一轮方案评审重点。")
        return ProductArchitectureBriefResponse(
            provider=self.provider_name,
            model=self.model_name,
            participants=["Product Manager", "Architect"],
            scenarioLabel="产品与架构协作简报",
            collaborationSummary="先锁首期方案边界，再把高风险点和开放问题送回现有治理模块继续收敛。",
            recommended_operator_action=build_product_architecture_operator_action(
                open_question_count=len(open_questions),
                risk_count=len(risks),
            ),
            governance_linkage={
                "targetModule": "project_center",
                "currentStageCode": payload.current_stage_code or "unknown",
                "projectId": payload.project_name or "",
                "recommendedArtifactType": "meeting_record",
            },
            roleInsights={
                "productManagerView": f"Product Manager：围绕“{payload.feature_summary or payload.requirement_summary or '核心业务链路'}”先锁首期可演示价值。",
                "architectView": "Architect：先确认系统边界、关键约束和实施顺序，避免后续大幅回撤。",
            },
            architectureFocusAreas=build_architecture_focus_areas(payload),
            deliveryImplications=build_delivery_implications(payload),
            projectGovernanceLinkageSummary="这份简报适合作为项目记录沉淀，并把高风险点拆回澄清中心、决策中心或后续设计评审。",
            followUpHints=build_product_architecture_follow_up_hints(
                risk_count=len(risks),
                open_question_count=len(open_questions),
            ),
            suggestion=ProductArchitectureBriefSuggestion(
                briefTitle=f"{project_name} 产品与架构协作简报",
                solutionBrief=solution_brief,
                risks=risks,
                openQuestions=open_questions,
                nextSteps=next_steps,
            ),
        )

    def summarize_meeting(
        self,
        payload: MeetingSummaryRequest,
    ) -> MeetingSummaryResponse:
        lines = [line.strip("-* \t") for line in payload.raw_notes.splitlines() if line.strip()]
        summary_lines = lines[:3] if lines else ["本次讨论围绕项目推进中的关键事项展开。"]
        summary = "；".join(summary_lines)

        action_items = [
            line for line in lines if any(token in line.lower() for token in ["todo", "action", "follow", "跟进", "确认"])
        ]
        if not action_items:
            action_items = [
                "整理本次讨论中的需求边界和待确认事项。",
                "根据结论更新澄清项、决策项或预算评估。",
            ]

        open_questions = [line for line in lines if "?" in line or "待确认" in line or "待补充" in line]
        if not open_questions:
            open_questions = ["是否已经形成可执行的范围、预算和排期确认结论？"]

        decision_candidates = [
            MeetingDecisionCandidate(
                title="确认需求范围边界",
                decisionType="requirement_confirmation",
                blockerFlag=True,
                recommendedOption="优先锁定首期 MVP 范围，次要能力进入下一阶段。",
            )
        ]
        if any("预算" in line for line in lines):
            decision_candidates.append(
                MeetingDecisionCandidate(
                    title="确认预算缓冲策略",
                    decisionType="budget_change",
                    blockerFlag=False,
                    recommendedOption="预留额外预算缓冲以覆盖范围和估算不确定性。",
                )
            )

        return MeetingSummaryResponse(
            provider=self.provider_name,
            model=self.model_name,
            suggestion=MeetingSummarySuggestion(
                meetingTitle=payload.meeting_title or "项目讨论纪要",
                summary=summary,
                actionItems=list(islice(action_items, 5)),
                openQuestions=list(islice(open_questions, 5)),
                decisionCandidates=list(islice(decision_candidates, 4)),
            ),
        )

    def _filled(self, value: str | None) -> bool:
        return value is not None and bool(value.strip())


def decision_type_requires_manual_approval(decision_type: str | None) -> bool:
    return (decision_type or "") in {"budget_change", "release", "scope_change"}


def build_decision_risk_flags(payload: DecisionBudgetReviewRequest) -> list[str]:
    flags: list[str] = []
    if payload.blocker_flag:
        flags.append("scope_drift")
    if not (payload.budget_range or "").strip():
        flags.append("budget_confirmation")
    if (payload.current_stage_code or "") in {"clarification", "feasibility", "estimation"}:
        flags.append("timeline_pressure")
    return flags


def build_architecture_focus_areas(payload: ProductArchitectureBriefRequest) -> list[str]:
    areas: list[str] = []
    if (payload.feature_summary or payload.requirement_summary or "").strip():
        areas.append("首期功能边界")
    if (payload.technical_constraints or "").strip():
        areas.append("技术与集成约束")
    if not areas:
        areas.append("方案边界与实施顺序")
    return areas


def build_delivery_implications(payload: ProductArchitectureBriefRequest) -> list[str]:
    implications = [
        "保存后将作为项目记录留档，便于后续设计评审、澄清和决策复用。",
        "不会自动触发审批、阶段流转或外部写入。",
    ]
    if (payload.focus_notes or "").strip():
        implications.append("操作员关注点已纳入简报语境，用于后续治理讨论。")
    return implications


def build_requirement_operator_action(
    blocker_count: int,
    escalation_count: int,
    follow_up_modules: list[str],
) -> str:
    if blocker_count > 0 and escalation_count > 0:
        return f"先应用 {blocker_count} 条阻塞澄清，再人工判断 {escalation_count} 条是否升级到决策中心。"
    if blocker_count > 0:
        return f"先应用 {blocker_count} 条阻塞澄清，补齐目标、范围和约束后再继续下一轮确认。"
    if "decision_center" in follow_up_modules:
        return "优先应用会影响范围、预算或里程碑承诺的事项，其余问题留待下一轮澄清。"
    return "先应用高影响澄清项，其余一般性问题保留在下一轮确认。"


def build_requirement_follow_up_hints(
    blocker_count: int,
    escalation_count: int,
    follow_up_modules: list[str],
) -> list[str]:
    hints: list[str] = []
    if blocker_count > 0:
        hints.append("阻塞项先进入澄清中心，避免直接进入细化估算。")
    if escalation_count > 0:
        hints.append("会改变范围、预算或时间承诺的项，建议人工判断后升级到决策中心。")
    if "clarification_center" in follow_up_modules:
        hints.append("输入型问题先补齐，不要过早展开方案讨论。")
    return unique_texts(hints)[:3]


def build_decision_budget_operator_action(budget_confirmation_advised: bool) -> str:
    if budget_confirmation_advised:
        return "先把推荐选项与预算影响写入当前决策，再人工判断是否补预算确认或审批。"
    return "先更新当前决策记录并留档预算说明，暂不需要自动推进审批。"


def build_decision_linkage_summary(payload: DecisionBudgetReviewRequest) -> str:
    if payload.blocker_flag:
        return "当前事项仍应保留在正式决策链路中，先确认边界再决定是否推进执行。"
    return "当前更适合在现有决策单内沉淀推荐选项，不建议另建平行确认记录。"


def build_budget_linkage_summary(
    payload: DecisionBudgetReviewRequest,
    budget_confirmation_advised: bool,
) -> str:
    if budget_confirmation_advised:
        return "建议先补预算确认说明；当前评审不会直接修改预算数字。"
    if (payload.budget_impact_summary or "").strip():
        return "当前已有预算说明，可继续沿用并补充更明确的资源影响描述。"
    return "当前以预算留档为主，无需自动触发预算调整。"


def build_decision_follow_up_hints(
    budget_confirmation_advised: bool,
    blocker_flag: bool,
) -> list[str]:
    hints = ["先把推荐选项、预算影响和项目影响写回当前决策单。"]
    if budget_confirmation_advised:
        hints.append("如预算边界仍未锁定，再人工判断是否进入审批中心。")
    if blocker_flag:
        hints.append("阻塞型事项在确认前不要默认进入执行阶段。")
    return unique_texts(hints)[:3]


def build_product_architecture_operator_action(
    open_question_count: int,
    risk_count: int,
) -> str:
    if risk_count > 0 or open_question_count > 0:
        return "先保存简报，再把风险最高或最不明确的 1-2 项送回澄清中心或决策中心。"
    return "先保存简报作为项目记录，再把结论带入下一轮方案或设计评审。"


def build_product_architecture_follow_up_hints(
    risk_count: int,
    open_question_count: int,
) -> list[str]:
    hints = ["这份简报适合作为项目记录，不应直接替代决策或审批记录。"]
    if risk_count > 0:
        hints.append("高风险点应继续拆回治理模块，不要只停留在会议纪要。")
    if open_question_count > 0:
        hints.append("开放问题建议在进入详细设计前完成第一轮确认。")
    return unique_texts(hints)[:3]


def unique_texts(values) -> list[str]:
    result: list[str] = []
    seen: set[str] = set()
    for value in values:
        if value is None:
            continue
        text = str(value).strip()
        if not text:
            continue
        key = "".join(ch.lower() for ch in text if ch.isalnum())
        if key in seen:
            continue
        seen.add(key)
        result.append(text)
    return result
