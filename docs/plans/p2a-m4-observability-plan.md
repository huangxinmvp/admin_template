# P2A-M4 Observability and Diagnostics Plan

## Goal

P2A-M4 improves pilot readiness by making failures easier to trace, classify, diagnose, and explain across:

- Spring Boot backend
- `agent_runtime`
- external integration calls

This milestone is not about a full observability platform. It is about practical operational visibility with minimal scope.

## Why This Milestone Now

After P2A-M3:

- the core browser walkthrough is repeatable
- evidence capture has a workable path
- Linear and Figma are verified for controlled real-environment demo use

The next operational problem is speed of diagnosis:

- when a flow fails, can an operator tell where it failed?
- can backend, runtime, and external calls be tied together by request id?
- can someone distinguish auth, target-not-found, payload, and network failures without guessing?

P2A-M4 should improve that visibility before P2A-M5 pilot-facing hardening.

## Scope

P2A-M4 covers:

- request/correlation id visibility improvements
- clearer failure classification for runtime, Linear, and Figma calls
- lightweight diagnostics counters and timing visibility
- operator-facing troubleshooting guidance

P2A-M4 does not cover:

- a full metrics stack
- log aggregation infrastructure
- distributed tracing infrastructure
- dashboards outside lightweight built-in diagnostics
- new product features

## Implemented Direction

### 1. Request correlation

Current request correlation now supports:

- backend request id filter and response header
- backend `Result` body carrying `requestId`
- runtime response header and diagnostics snapshot carrying `requestId`
- outbound request propagation through `X-Request-Id`

### 2. Error classification

Current integration diagnostics now distinguish common categories such as:

- `auth`
- `payload_invalid`
- `target_not_found`
- `permission_or_team`
- `upstream_unavailable`
- `network`
- `exception`

### 3. Lightweight diagnostics

Current lightweight diagnostics now include:

- backend in-memory success/failure counters for:
  - `agent_runtime`
  - `linear`
  - `figma`
- last failure summaries with request id and message
- timing visibility via last and average duration
- runtime in-memory per-path request counters and last failures

### 4. Diagnostics endpoints

Current lightweight endpoints:

- backend: `GET /api/system/diagnostics`
- runtime: `GET /health/diagnostics`

These are intentionally lightweight and suitable for pilot troubleshooting, not long-term historical observability.

## Acceptance Criteria

P2A-M4 is complete when:

- request ids are easier to see in error responses
- lightweight backend diagnostics exist for runtime and integration calls
- runtime diagnostics expose per-path counters
- common integration failures are documented with actionable checks
- operators have a runbook for what to inspect first when something breaks

## Validation Plan

Validation for this milestone is targeted and practical:

- confirm backend responses include `requestId`
- confirm backend diagnostics endpoint can report integration activity
- confirm runtime diagnostics endpoint can report per-path activity
- confirm targeted unit tests still pass for Linear/Figma/runtime integration code
- confirm docs match the actually implemented endpoints and classification behavior

## Remaining Gaps Before P2A-M5

After P2A-M4, the main gaps before P2A-M5 will still include:

- diagnostics are in-memory rather than historical
- no centralized log search or log aggregation
- no alerting
- no operator UI for diagnostics beyond direct endpoint access
- no end-to-end root-cause summary page in the product
