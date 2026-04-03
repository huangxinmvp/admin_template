from __future__ import annotations

from agent_runtime.config import RuntimeSettings
from agent_runtime.providers import MockSuggestionProvider, OpenAICompatibleSuggestionProvider
from agent_runtime.schemas import (
    BudgetImpactSuggestionRequest,
    BudgetImpactSuggestionResponse,
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


class AgentRuntimeService:
    def __init__(self, settings: RuntimeSettings) -> None:
        self._settings = settings

    def generate_clarifications(
        self,
        payload: ClarificationGenerationRequest,
    ) -> ClarificationGenerationResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.generate_clarifications(payload)

    def suggest_decision_promotions(
        self,
        payload: DecisionPromotionSuggestionRequest,
    ) -> DecisionPromotionSuggestionResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.suggest_decision_promotions(payload)

    def suggest_budget_impact(
        self,
        payload: BudgetImpactSuggestionRequest,
    ) -> BudgetImpactSuggestionResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.suggest_budget_impact(payload)

    def review_requirement_clarifications(
        self,
        payload: RequirementClarificationReviewRequest,
    ) -> RequirementClarificationReviewResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.review_requirement_clarifications(payload)

    def review_decision_budget(
        self,
        payload: DecisionBudgetReviewRequest,
    ) -> DecisionBudgetReviewResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.review_decision_budget(payload)

    def generate_product_architecture_brief(
        self,
        payload: ProductArchitectureBriefRequest,
    ) -> ProductArchitectureBriefResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.generate_product_architecture_brief(payload)

    def summarize_meeting(
        self,
        payload: MeetingSummaryRequest,
    ) -> MeetingSummaryResponse:
        provider = self._resolve_provider(payload.meta.provider_hint if payload.meta else None, payload.meta.model_hint if payload.meta else None)
        return provider.summarize_meeting(payload)

    def _resolve_provider(self, provider_hint: str | None, model_hint: str | None):
        provider_name = (provider_hint or self._settings.default_provider or "mock").strip().lower()
        model_name = (model_hint or self._settings.default_model or "mock-suggestion-v1").strip()
        if provider_name == "mock":
            return MockSuggestionProvider(model_name=model_name)
        if provider_name == "openai_compatible":
            try:
                return OpenAICompatibleSuggestionProvider(
                    base_url=self._settings.openai_compatible_base_url,
                    api_key=self._settings.openai_compatible_api_key,
                    model_name=model_name,
                    timeout_seconds=self._settings.timeout_seconds,
                )
            except Exception:
                if not self._settings.allow_mock_fallback:
                    raise
                return MockSuggestionProvider(model_name=self._settings.default_model)
        if self._settings.allow_mock_fallback:
            return MockSuggestionProvider(model_name=self._settings.default_model)
        raise ValueError(f"Unsupported provider: {provider_name}")
