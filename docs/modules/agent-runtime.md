# Agent Runtime

M12 adds the first minimal `agent_runtime/` service to AICoOS. It is a lightweight FastAPI runtime that provides structured AI assistance, while the Spring Boot backend remains the source of truth for governance records, approvals, budgets, and project state.

## Role In The Architecture

- `agent_runtime/` is a separate in-repo service
- Spring Boot calls it through a typed runtime client
- the frontend never calls it directly
- it returns suggestions only and does not directly write high-impact business records

## Supported Suggestion APIs

- `POST /api/v1/clarifications/generate`
- `POST /api/v1/decisions/promotion-suggestions`
- `POST /api/v1/budget/impact-suggestions`
- `POST /api/v1/meetings/summarize`
- `GET /health`

Each endpoint returns structured payloads that the main backend translates into operator-facing preview flows.

## M12 UI Integration

- Requirement Intake: generate clarification suggestions from saved intake context and apply selected items into `ClarificationItem`
- Clarification Center: analyze open clarification items and explicitly promote selected ones into `DecisionItem`
- Decision Center: generate budget impact suggestions for a decision and explicitly apply the summary to the decision record
- Project Center: paste meeting/discussion notes, preview a structured summary, then save it as `MeetingRecord`

## Configuration

M12 reuses the existing system config center with these keys:

- `agentRuntime.enabled`
- `agentRuntime.baseUrl`
- `agentRuntime.apiKey`
- `agentRuntime.timeoutMs`
- `agentRuntime.provider`
- `agentRuntime.model`
- `agentRuntime.mockMode`

These settings control Spring Boot -> runtime connectivity and the default runtime hinting behavior.

## Local Operation

- local runbook: [local-agent-runtime-e2e.md](/Users/hx/it_company/agent_company/docs/runbooks/local-agent-runtime-e2e.md)
- manual verification checklist: [m12-e2e-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m12-e2e-checklist.md)
- runtime smoke helper: [runtime_smoke_test.sh](/Users/hx/it_company/agent_company/agent_runtime/scripts/runtime_smoke_test.sh)

## Config Hygiene

- `agentRuntime.apiKey` is now treated as a password-style config in the config center
- the config center no longer re-displays the current stored value
- leaving the field blank during save keeps the existing value unchanged
- the more sensitive model-provider key should stay in runtime environment variables, not in Spring Boot system config
- current limitation: if you do save `agentRuntime.apiKey` into the system-config table, it is still stored in plaintext at rest

## Safety Rules

- no automatic approvals
- no automatic stage transitions
- no automatic budget recalculation
- no autonomous multi-agent execution
- no direct frontend -> runtime access

The runtime is intentionally intelligence-only in M12. Governance authority remains in the main system.
