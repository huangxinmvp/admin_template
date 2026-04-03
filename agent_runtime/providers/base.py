from __future__ import annotations

from typing import Protocol

from agent_runtime.schemas import (
    BudgetImpactSuggestionResponse,
    BudgetImpactSuggestionRequest,
    ClarificationGenerationRequest,
    ClarificationGenerationResponse,
    DecisionBudgetReviewRequest,
    DecisionBudgetReviewResponse,
    DecisionPromotionSuggestionRequest,
    DecisionPromotionSuggestionResponse,
    MeetingSummaryRequest,
    MeetingSummaryResponse,
    ProductArchitectureBriefRequest,
    ProductArchitectureBriefResponse,
    RequirementClarificationReviewRequest,
    RequirementClarificationReviewResponse,
)


class SuggestionProvider(Protocol):
    @property
    def provider_name(self) -> str: ...

    @property
    def model_name(self) -> str: ...

    def generate_clarifications(
        self,
        payload: ClarificationGenerationRequest,
    ) -> ClarificationGenerationResponse: ...

    def suggest_decision_promotions(
        self,
        payload: DecisionPromotionSuggestionRequest,
    ) -> DecisionPromotionSuggestionResponse: ...

    def suggest_budget_impact(
        self,
        payload: BudgetImpactSuggestionRequest,
    ) -> BudgetImpactSuggestionResponse: ...

    def review_requirement_clarifications(
        self,
        payload: RequirementClarificationReviewRequest,
    ) -> RequirementClarificationReviewResponse: ...

    def review_decision_budget(
        self,
        payload: DecisionBudgetReviewRequest,
    ) -> DecisionBudgetReviewResponse: ...

    def generate_product_architecture_brief(
        self,
        payload: ProductArchitectureBriefRequest,
    ) -> ProductArchitectureBriefResponse: ...

    def summarize_meeting(
        self,
        payload: MeetingSummaryRequest,
    ) -> MeetingSummaryResponse: ...
