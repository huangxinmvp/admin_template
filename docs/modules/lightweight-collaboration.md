# Lightweight Collaboration

## Purpose
- M14 adds a small, fixed set of multi-role collaboration previews to make AICoOS feel more like a governed AI company.
- All collaboration remains suggestion-first.
- Spring Boot stays the source of truth.
- No autonomous execution, background workers, or automatic external writes are introduced.

## Collaboration Scenarios

### 1. Requirement Clarification Review
- Roles: `Requirement Analyst` + `Product Manager`
- Runtime output:
  - merged clarification suggestions
  - blocker assessment
  - next questions
- UI entry points:
  - `Requirement Intake`
  - `Clarification Center`
- Apply behavior:
  - operators explicitly select suggestions
  - selected items are created as `ClarificationItem`
  - no record is auto-created during preview

### 2. Decision + Budget Review
- Roles: `Product Manager` + `Budget Analyst`
- Runtime output:
  - decision recommendation
  - budget impact note
  - whether budget confirmation is advised
  - recommended option
  - project impact note
  - blocker assessment
  - next steps
- UI entry point:
  - `Decision Center` detail drawer
- Apply behavior:
  - operators explicitly apply the review result
  - the existing `DecisionItem` is updated with the suggested notes
  - no decision status change, approval trigger, or budget recalculation happens automatically

### 3. Product + Architecture Brief
- Roles: `Product Manager` + `Architect`
- Runtime output:
  - lightweight solution brief
  - risks
  - open questions
  - recommended next steps
- UI entry point:
  - `Project Center` detail drawer
- Apply behavior:
  - operators explicitly save the brief
  - the brief is stored as a `MeetingRecord`
  - this keeps the artifact auditable without adding a new heavyweight module

## Runtime API
- `POST /api/v1/collaboration/requirement-clarification-review`
- `POST /api/v1/collaboration/decision-budget-review`
- `POST /api/v1/collaboration/product-architecture-brief`

## Backend Integration
- Runtime integration stays inside the existing M12 client/service layer.
- Spring Boot wraps runtime results behind operator-facing endpoints:
  - `POST /api/aicoos/requirementIntake/project/{projectId}/clarification-collaboration`
  - `POST /api/aicoos/decisionItem/{id}/decision-budget-review`
  - `POST /api/aicoos/decisionItem/{id}/decision-budget-review/apply`
  - `POST /api/aicoos/meetingRecord/project/{projectId}/product-architecture-brief`
- Existing write paths are reused:
  - clarification apply
  - decision update
  - meeting record save

## UI Surface
- `Requirement Intake`: adds `协作澄清审阅`
- `Clarification Center`: adds `协作澄清审阅`
- `Decision Center`: adds `协作评审`
- `Project Center`: adds `产品方案简报`

## Safety Boundaries
- Collaboration previews never auto-create or auto-confirm high-impact records.
- Operators must always click an explicit apply/save action.
- No approval automation, stage transition, external write automation, or agent-runtime orchestration is added in M14.
- The collaboration layer is fixed-scenario only and is not a generic multi-agent framework.
