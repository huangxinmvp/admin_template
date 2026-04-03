from __future__ import annotations

from pydantic import BaseModel, Field


class RequestMeta(BaseModel):
    provider_hint: str | None = Field(default=None, alias="providerHint")
    model_hint: str | None = Field(default=None, alias="modelHint")

    model_config = {"populate_by_name": True}


class ClarificationGenerationRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    project_type: str | None = Field(default=None, alias="projectType")
    business_goal: str | None = Field(default=None, alias="businessGoal")
    feature_summary: str | None = Field(default=None, alias="featureSummary")
    reference_products: str | None = Field(default=None, alias="referenceProducts")
    timeline_expectation: str | None = Field(default=None, alias="timelineExpectation")
    budget_range: str | None = Field(default=None, alias="budgetRange")
    technical_constraints: str | None = Field(default=None, alias="technicalConstraints")
    notes: str | None = None
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class ClarificationSuggestion(BaseModel):
    title: str
    question: str
    category: str
    severity: str
    suggested_options: str = Field(alias="suggestedOptions")
    blocker_flag: bool = Field(alias="blockerFlag")
    reason: str
    governance_reason: str | None = Field(default=None, alias="governanceReason")
    follow_up_module: str | None = Field(default=None, alias="followUpModule")
    escalation_recommended: bool | None = Field(default=None, alias="escalationRecommended")

    model_config = {"populate_by_name": True}


class ClarificationGenerationResponse(BaseModel):
    provider: str
    model: str
    suggestions: list[ClarificationSuggestion]


class ClarificationContext(BaseModel):
    clarification_id: str = Field(alias="clarificationId")
    title: str | None = None
    question: str | None = None
    category: str | None = None
    severity: str | None = None
    suggested_options: str | None = Field(default=None, alias="suggestedOptions")
    user_response: str | None = Field(default=None, alias="userResponse")
    status: str | None = None

    model_config = {"populate_by_name": True}


class DecisionPromotionSuggestionRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    project_type: str | None = Field(default=None, alias="projectType")
    current_stage_code: str | None = Field(default=None, alias="currentStageCode")
    clarifications: list[ClarificationContext] = Field(default_factory=list)
    requirement_summary: str | None = Field(default=None, alias="requirementSummary")
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class DecisionPromotionSuggestion(BaseModel):
    clarification_id: str = Field(alias="clarificationId")
    suggested_title: str = Field(alias="suggestedTitle")
    decision_type: str = Field(alias="type")
    impact_summary: str = Field(alias="impactSummary")
    suggested_options: str = Field(alias="suggestedOptions")
    recommended_option: str = Field(alias="recommendedOption")
    blocker_flag: bool = Field(alias="blockerFlag")
    reason: str

    model_config = {"populate_by_name": True}


class DecisionPromotionSuggestionResponse(BaseModel):
    provider: str
    model: str
    suggestions: list[DecisionPromotionSuggestion]


class BudgetImpactSuggestionRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    project_type: str | None = Field(default=None, alias="projectType")
    current_stage_code: str | None = Field(default=None, alias="currentStageCode")
    budget_range: str | None = Field(default=None, alias="budgetRange")
    decision_title: str | None = Field(default=None, alias="decisionTitle")
    decision_type: str | None = Field(default=None, alias="decisionType")
    decision_description: str | None = Field(default=None, alias="decisionDescription")
    decision_impact_summary: str | None = Field(default=None, alias="decisionImpactSummary")
    requirement_summary: str | None = Field(default=None, alias="requirementSummary")
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class BudgetImpactSuggestion(BaseModel):
    budget_impact_summary: str = Field(alias="budgetImpactSummary")
    project_impact_summary: str = Field(alias="projectImpactSummary")
    delta_range: str = Field(alias="deltaRange")
    affected_roles: list[str] = Field(default_factory=list, alias="affectedRoles")
    confidence: str

    model_config = {"populate_by_name": True}


class BudgetImpactSuggestionResponse(BaseModel):
    provider: str
    model: str
    suggestion: BudgetImpactSuggestion


class MeetingSummaryRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    current_stage_code: str | None = Field(default=None, alias="currentStageCode")
    meeting_title: str | None = Field(default=None, alias="meetingTitle")
    raw_notes: str = Field(alias="rawNotes", min_length=1)
    source_type: str | None = Field(default=None, alias="sourceType")
    source_object_id: str | None = Field(default=None, alias="sourceObjectId")
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class MeetingDecisionCandidate(BaseModel):
    title: str
    decision_type: str = Field(alias="decisionType")
    blocker_flag: bool = Field(alias="blockerFlag")
    recommended_option: str | None = Field(default=None, alias="recommendedOption")

    model_config = {"populate_by_name": True}


class MeetingSummarySuggestion(BaseModel):
    meeting_title: str = Field(alias="meetingTitle")
    summary: str
    action_items: list[str] = Field(default_factory=list, alias="actionItems")
    open_questions: list[str] = Field(default_factory=list, alias="openQuestions")
    decision_candidates: list[MeetingDecisionCandidate] = Field(
        default_factory=list,
        alias="decisionCandidates",
    )

    model_config = {"populate_by_name": True}


class MeetingSummaryResponse(BaseModel):
    provider: str
    model: str
    suggestion: MeetingSummarySuggestion


class RequirementClarificationReviewRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    project_type: str | None = Field(default=None, alias="projectType")
    current_stage_code: str | None = Field(default=None, alias="currentStageCode")
    business_goal: str | None = Field(default=None, alias="businessGoal")
    feature_summary: str | None = Field(default=None, alias="featureSummary")
    reference_products: str | None = Field(default=None, alias="referenceProducts")
    timeline_expectation: str | None = Field(default=None, alias="timelineExpectation")
    budget_range: str | None = Field(default=None, alias="budgetRange")
    technical_constraints: str | None = Field(default=None, alias="technicalConstraints")
    notes: str | None = None
    existing_clarification_count: int | None = Field(default=None, alias="existingClarificationCount")
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class RequirementClarificationReviewResponse(BaseModel):
    provider: str
    model: str
    participants: list[str] = Field(default_factory=list)
    scenario_label: str = Field(alias="scenarioLabel")
    collaboration_summary: str = Field(alias="collaborationSummary")
    recommended_operator_action: str = Field(alias="recommendedOperatorAction")
    governance_linkage: dict = Field(alias="governanceLinkage")
    role_insights: list[str] = Field(default_factory=list, alias="roleInsights")
    selection_guidance: str = Field(alias="selectionGuidance")
    governance_interpretation: str = Field(alias="governanceInterpretation")
    decision_escalation_advised: bool = Field(alias="decisionEscalationAdvised")
    follow_up_hints: list[str] = Field(default_factory=list, alias="followUpHints")
    blocker_assessment: str = Field(alias="blockerAssessment")
    next_questions: list[str] = Field(default_factory=list, alias="nextQuestions")
    suggestions: list[ClarificationSuggestion]

    model_config = {"populate_by_name": True}


class DecisionBudgetReviewRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    project_type: str | None = Field(default=None, alias="projectType")
    current_stage_code: str | None = Field(default=None, alias="currentStageCode")
    budget_range: str | None = Field(default=None, alias="budgetRange")
    decision_title: str | None = Field(default=None, alias="decisionTitle")
    decision_type: str | None = Field(default=None, alias="decisionType")
    decision_description: str | None = Field(default=None, alias="decisionDescription")
    decision_impact_summary: str | None = Field(default=None, alias="decisionImpactSummary")
    suggested_options: str | None = Field(default=None, alias="suggestedOptions")
    recommended_option: str | None = Field(default=None, alias="recommendedOption")
    blocker_flag: bool | None = Field(default=None, alias="blockerFlag")
    budget_impact_summary: str | None = Field(default=None, alias="budgetImpactSummary")
    project_impact_summary: str | None = Field(default=None, alias="projectImpactSummary")
    requirement_summary: str | None = Field(default=None, alias="requirementSummary")
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class DecisionBudgetReviewSuggestion(BaseModel):
    decision_recommendation: str = Field(alias="decisionRecommendation")
    budget_impact_note: str = Field(alias="budgetImpactNote")
    budget_confirmation_advised: bool = Field(alias="budgetConfirmationAdvised")
    recommended_option: str = Field(alias="recommendedOption")
    project_impact_note: str = Field(alias="projectImpactNote")
    blocker_assessment: str = Field(alias="blockerAssessment")
    next_steps: list[str] = Field(default_factory=list, alias="nextSteps")

    model_config = {"populate_by_name": True}


class DecisionBudgetReviewResponse(BaseModel):
    provider: str
    model: str
    participants: list[str] = Field(default_factory=list)
    scenario_label: str = Field(alias="scenarioLabel")
    collaboration_summary: str = Field(alias="collaborationSummary")
    recommended_operator_action: str = Field(alias="recommendedOperatorAction")
    governance_linkage: dict = Field(alias="governanceLinkage")
    role_insights: dict = Field(alias="roleInsights")
    risk_flags: list[str] = Field(default_factory=list, alias="riskFlags")
    decision_linkage_summary: str = Field(alias="decisionLinkageSummary")
    budget_linkage_summary: str = Field(alias="budgetLinkageSummary")
    follow_up_hints: list[str] = Field(default_factory=list, alias="followUpHints")
    suggestion: DecisionBudgetReviewSuggestion

    model_config = {"populate_by_name": True}


class ProductArchitectureBriefRequest(BaseModel):
    project_name: str | None = Field(default=None, alias="projectName")
    project_type: str | None = Field(default=None, alias="projectType")
    current_stage_code: str | None = Field(default=None, alias="currentStageCode")
    business_goal: str | None = Field(default=None, alias="businessGoal")
    feature_summary: str | None = Field(default=None, alias="featureSummary")
    technical_constraints: str | None = Field(default=None, alias="technicalConstraints")
    requirement_summary: str | None = Field(default=None, alias="requirementSummary")
    focus_notes: str | None = Field(default=None, alias="focusNotes")
    meta: RequestMeta | None = None

    model_config = {"populate_by_name": True}


class ProductArchitectureBriefSuggestion(BaseModel):
    brief_title: str = Field(alias="briefTitle")
    solution_brief: str = Field(alias="solutionBrief")
    risks: list[str] = Field(default_factory=list)
    open_questions: list[str] = Field(default_factory=list, alias="openQuestions")
    next_steps: list[str] = Field(default_factory=list, alias="nextSteps")

    model_config = {"populate_by_name": True}


class ProductArchitectureBriefResponse(BaseModel):
    provider: str
    model: str
    participants: list[str] = Field(default_factory=list)
    scenario_label: str = Field(alias="scenarioLabel")
    collaboration_summary: str = Field(alias="collaborationSummary")
    recommended_operator_action: str = Field(alias="recommendedOperatorAction")
    governance_linkage: dict = Field(alias="governanceLinkage")
    role_insights: dict = Field(alias="roleInsights")
    architecture_focus_areas: list[str] = Field(default_factory=list, alias="architectureFocusAreas")
    delivery_implications: list[str] = Field(default_factory=list, alias="deliveryImplications")
    project_governance_linkage_summary: str = Field(alias="projectGovernanceLinkageSummary")
    follow_up_hints: list[str] = Field(default_factory=list, alias="followUpHints")
    suggestion: ProductArchitectureBriefSuggestion

    model_config = {"populate_by_name": True}
