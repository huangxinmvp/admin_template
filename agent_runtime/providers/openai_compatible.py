from __future__ import annotations

import json
import uuid

import httpx

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


class OpenAICompatibleSuggestionProvider:
    _MAX_ATTEMPTS = 2

    def __init__(
        self,
        base_url: str,
        api_key: str,
        model_name: str,
        timeout_seconds: float,
    ) -> None:
        if not base_url or not api_key:
            raise ValueError("OpenAI-compatible provider requires base_url and api_key")
        self._base_url = base_url.rstrip("/")
        self._api_key = api_key
        self._model_name = model_name
        self._client = httpx.Client(timeout=timeout_seconds)

    @property
    def provider_name(self) -> str:
        return "openai_compatible"

    @property
    def model_name(self) -> str:
        return self._model_name

    def generate_clarifications(
        self,
        payload: ClarificationGenerationRequest,
    ) -> ClarificationGenerationResponse:
        content = self._complete_json(
            system_prompt=(
                "You generate structured clarification suggestions for enterprise software delivery. "
                "Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return ClarificationGenerationResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def suggest_decision_promotions(
        self,
        payload: DecisionPromotionSuggestionRequest,
    ) -> DecisionPromotionSuggestionResponse:
        content = self._complete_json(
            system_prompt=(
                "You analyze clarification items and return which ones should be promoted "
                "to formal decision items. Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return DecisionPromotionSuggestionResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def suggest_budget_impact(
        self,
        payload: BudgetImpactSuggestionRequest,
    ) -> BudgetImpactSuggestionResponse:
        content = self._complete_json(
            system_prompt=(
                "You generate lightweight budget impact suggestions for governed software delivery. "
                "Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return BudgetImpactSuggestionResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def review_requirement_clarifications(
        self,
        payload: RequirementClarificationReviewRequest,
    ) -> RequirementClarificationReviewResponse:
        content = self._complete_json(
            system_prompt=(
                "You simulate a collaboration between a Requirement Analyst and a Product Manager. "
                "Return a JSON object with scenarioLabel, collaborationSummary, recommendedOperatorAction, "
                "governanceLinkage, roleInsights, selectionGuidance, governanceInterpretation, "
                "decisionEscalationAdvised, followUpHints, participants, blockerAssessment, "
                "nextQuestions, and suggestions. Each suggestion must follow the existing clarification suggestion "
                "structure and include governanceReason, followUpModule, and escalationRecommended when applicable. "
                "Keep collaborationSummary to one short sentence. Make roleInsights clearly different by role. "
                "Make recommendedOperatorAction concrete and operator-ready. "
                "Do not repeat the same point across summary, role insights, follow-up hints, and questions. "
                "Prefer 2-3 high-value followUpHints and nextQuestions. "
                "Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return RequirementClarificationReviewResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def review_decision_budget(
        self,
        payload: DecisionBudgetReviewRequest,
    ) -> DecisionBudgetReviewResponse:
        content = self._complete_json(
            system_prompt=(
                "You simulate a collaboration between a Product Manager and a Budget Analyst. "
                "Return a JSON object with scenarioLabel, collaborationSummary, recommendedOperatorAction, "
                "governanceLinkage, roleInsights, riskFlags, decisionLinkageSummary, budgetLinkageSummary, "
                "followUpHints, participants and a suggestion containing "
                "decisionRecommendation, budgetImpactNote, budgetConfirmationAdvised, recommendedOption, "
                "projectImpactNote, blockerAssessment, and nextSteps. "
                "Keep collaborationSummary to one short sentence. "
                "Make Product Manager and Budget Analyst views clearly different. "
                "Make recommendedOperatorAction explicit about what should be applied now versus what still needs manual judgment. "
                "Avoid repeating the same sentence in roleInsights, linkage summaries, followUpHints, and nextSteps. "
                "Prefer 2-3 high-signal nextSteps only. Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return DecisionBudgetReviewResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def generate_product_architecture_brief(
        self,
        payload: ProductArchitectureBriefRequest,
    ) -> ProductArchitectureBriefResponse:
        content = self._complete_json(
            system_prompt=(
                "You simulate a collaboration between a Product Manager and an Architect. "
                "Return a JSON object with scenarioLabel, collaborationSummary, recommendedOperatorAction, "
                "governanceLinkage, roleInsights, architectureFocusAreas, deliveryImplications, "
                "projectGovernanceLinkageSummary, followUpHints, participants "
                "and a suggestion containing briefTitle, solutionBrief, risks, openQuestions, and nextSteps. "
                "Keep collaborationSummary to one short sentence. "
                "Make Product Manager and Architect views clearly different. "
                "Make recommendedOperatorAction concrete about save plus follow-up handling. "
                "Avoid low-value repetition across summary, roleInsights, governance linkage summary, followUpHints, and nextSteps. "
                "Prefer short, operator-useful lists with 2-3 strong items. "
                "Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return ProductArchitectureBriefResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def summarize_meeting(
        self,
        payload: MeetingSummaryRequest,
    ) -> MeetingSummaryResponse:
        content = self._complete_json(
            system_prompt=(
                "You summarize project discussion notes into structured meeting summaries "
                "with action items and decision candidates. Respond with JSON only."
            ),
            user_payload=payload.model_dump(by_alias=True, exclude_none=True),
        )
        return MeetingSummaryResponse.model_validate({
            "provider": self.provider_name,
            "model": self.model_name,
            **content,
        })

    def _complete_json(self, system_prompt: str, user_payload: dict) -> dict:
        request_id = str(uuid.uuid4())
        payload = {
            "model": self._model_name,
            "temperature": 0.2,
            "response_format": {"type": "json_object"},
            "messages": [
                {"role": "system", "content": system_prompt},
                {
                    "role": "user",
                    "content": json.dumps(
                        {
                            "task": "Return a single JSON object matching the requested output schema.",
                            "input": user_payload,
                        },
                        ensure_ascii=False,
                    ),
                },
            ],
        }
        last_exception: Exception | None = None
        for attempt in range(1, self._MAX_ATTEMPTS + 1):
            try:
                response = self._client.post(
                    f"{self._base_url}/chat/completions",
                    headers={
                        "Authorization": f"Bearer {self._api_key}",
                        "Content-Type": "application/json",
                        "X-Request-Id": request_id,
                    },
                    json=payload,
                )
                if response.status_code >= 500 or response.status_code in {408, 429}:
                    if attempt < self._MAX_ATTEMPTS:
                        continue
                response.raise_for_status()
                body = response.json()
                message = ((body.get("choices") or [{}])[0].get("message") or {}).get("content") or "{}"
                return json.loads(self._strip_code_fence(message))
            except (httpx.TimeoutException, httpx.TransportError) as exc:
                last_exception = exc
                if attempt < self._MAX_ATTEMPTS:
                    continue
                raise
            except httpx.HTTPStatusError as exc:
                last_exception = exc
                if attempt < self._MAX_ATTEMPTS and exc.response.status_code >= 500:
                    continue
                raise
        if last_exception is not None:
            raise last_exception
        raise RuntimeError("OpenAI-compatible completion failed without response")

    def _strip_code_fence(self, text: str) -> str:
        value = text.strip()
        if value.startswith("```"):
            parts = value.split("```")
            if len(parts) >= 3:
                value = parts[1]
                if value.startswith("json"):
                    value = value[4:]
        return value.strip()
