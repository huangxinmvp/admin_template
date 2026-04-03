# M12 Agent Runtime Plan

M12 introduces a separate `agent_runtime/` FastAPI service and keeps Spring Boot as the only frontend-facing write path. The runtime returns structured suggestions for clarification generation, decision promotion analysis, budget impact estimation, and meeting summary generation, while operators still explicitly confirm any record creation or update in the main system.

## Implementation Focus

- Add a lightweight runtime service with `mock` and `openai_compatible` providers plus typed endpoints for clarification, decision, budget, and meeting suggestions.
- Add a Java 21 `HttpClient` integration layer in the Phase 1 backend, using `agentRuntime.*` system-config keys for runtime address, key, timeout, provider, model, and mock mode.
- Reuse existing AICoOS pages:
  - Requirement Intake: generate clarification suggestions and explicitly apply selected items
  - Clarification Center: analyze which clarification items should be promoted into decision items
  - Decision Center: generate and explicitly apply budget impact suggestions to a decision item
  - Project Center: generate meeting summaries from free-text notes and save them as `MeetingRecord`
- Add a minimal `MeetingRecord` business object so meeting outputs are persisted in the main governed system instead of staying transient.

## Validation Targets

- Runtime schema/unit checks via `python3 -m unittest discover agent_runtime/tests`
- Backend compile and targeted tests for runtime client and suggestion flows
- Frontend `npm run tsc`
- Manual verification that suggestions are previewed first and only create/update records after explicit confirmation

## Assumptions

- Runtime remains synchronous and suggestion-first in M12.
- Frontend never calls the runtime directly.
- Governance state still recomputes only through the Spring Boot write path.
