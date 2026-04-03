from __future__ import annotations

import uuid
import time

from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import JSONResponse
from starlette.requests import Request
from starlette.responses import Response

from agent_runtime.auth import require_runtime_key
from agent_runtime.config import load_settings
from agent_runtime.diagnostics import runtime_diagnostics
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
from agent_runtime.service import AgentRuntimeService


def create_app() -> FastAPI:
    app = FastAPI(title="AICoOS Agent Runtime", version="0.1.0")

    @app.middleware("http")
    async def request_correlation_middleware(request: Request, call_next) -> Response:
        request_id = request.headers.get("X-Request-Id") or str(uuid.uuid4())
        request.state.request_id = request_id
        started_at = time.perf_counter()
        response = await call_next(request)
        response.headers["X-Request-Id"] = request_id
        runtime_diagnostics.record(
            path=request.url.path,
            success=response.status_code < 400,
            duration_ms=int((time.perf_counter() - started_at) * 1000),
            classification=classify_status(response.status_code),
            request_id=request_id,
            message=None if response.status_code < 400 else f"HTTP {response.status_code}",
        )
        return response

    @app.exception_handler(HTTPException)
    async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
        request_id = getattr(request.state, "request_id", str(uuid.uuid4()))
        return JSONResponse(
            status_code=exc.status_code,
            content={
                "detail": exc.detail,
                "requestId": request_id,
                "classification": classify_status(exc.status_code),
            },
            headers={"X-Request-Id": request_id},
        )

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
        request_id = getattr(request.state, "request_id", str(uuid.uuid4()))
        return JSONResponse(
            status_code=500,
            content={
                "detail": "internal runtime error",
                "requestId": request_id,
                "classification": "runtime_exception",
            },
            headers={"X-Request-Id": request_id},
        )

    @app.get("/health")
    def health() -> JSONResponse:
        settings = load_settings()
        ready = settings.runtime_key_configured() and settings.default_provider_ready()
        return JSONResponse(
            status_code=200 if ready else 503,
            content={
                "status": "ok" if ready else "degraded",
                "runtimeKeyConfigured": settings.runtime_key_configured(),
                "defaultProviderReady": settings.default_provider_ready(),
                "mockFallbackEnabled": settings.allow_mock_fallback,
            },
        )

    @app.get("/health/diagnostics", dependencies=[Depends(require_runtime_key)])
    def diagnostics(request: Request) -> dict:
        return runtime_diagnostics.snapshot(getattr(request.state, "request_id", None))

    @app.post(
        "/api/v1/clarifications/generate",
        response_model=ClarificationGenerationResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def generate_clarifications(
        payload: ClarificationGenerationRequest,
    ) -> ClarificationGenerationResponse:
        return AgentRuntimeService(load_settings()).generate_clarifications(payload)

    @app.post(
        "/api/v1/decisions/promotion-suggestions",
        response_model=DecisionPromotionSuggestionResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def decision_suggestions(
        payload: DecisionPromotionSuggestionRequest,
    ) -> DecisionPromotionSuggestionResponse:
        return AgentRuntimeService(load_settings()).suggest_decision_promotions(payload)

    @app.post(
        "/api/v1/budget/impact-suggestions",
        response_model=BudgetImpactSuggestionResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def budget_suggestions(
        payload: BudgetImpactSuggestionRequest,
    ) -> BudgetImpactSuggestionResponse:
        return AgentRuntimeService(load_settings()).suggest_budget_impact(payload)

    @app.post(
        "/api/v1/collaboration/requirement-clarification-review",
        response_model=RequirementClarificationReviewResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def review_requirement_clarifications(
        payload: RequirementClarificationReviewRequest,
    ) -> RequirementClarificationReviewResponse:
        return AgentRuntimeService(load_settings()).review_requirement_clarifications(payload)

    @app.post(
        "/api/v1/collaboration/decision-budget-review",
        response_model=DecisionBudgetReviewResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def review_decision_budget(
        payload: DecisionBudgetReviewRequest,
    ) -> DecisionBudgetReviewResponse:
        return AgentRuntimeService(load_settings()).review_decision_budget(payload)

    @app.post(
        "/api/v1/collaboration/product-architecture-brief",
        response_model=ProductArchitectureBriefResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def generate_product_architecture_brief(
        payload: ProductArchitectureBriefRequest,
    ) -> ProductArchitectureBriefResponse:
        return AgentRuntimeService(load_settings()).generate_product_architecture_brief(payload)

    @app.post(
        "/api/v1/meetings/summarize",
        response_model=MeetingSummaryResponse,
        dependencies=[Depends(require_runtime_key)],
    )
    def summarize_meeting(
        payload: MeetingSummaryRequest,
    ) -> MeetingSummaryResponse:
        return AgentRuntimeService(load_settings()).summarize_meeting(payload)

    return app


app = create_app()


def classify_status(status_code: int) -> str:
    if status_code in {401, 403}:
        return "auth"
    if status_code == 404:
        return "target_not_found"
    if status_code in {400, 422}:
        return "payload_invalid"
    if status_code >= 500:
        return "upstream_unavailable"
    if 200 <= status_code < 400:
        return "success"
    return "unknown"
