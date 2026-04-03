# M14 Lightweight Collaboration Plan

## Scope
- Extend `agent_runtime` with three fixed collaboration scenarios only:
  - requirement clarification review
  - decision + budget review
  - product + architecture brief
- Keep Spring Boot as the only trusted write path and expose collaboration results as preview-first operator tools.

## Backend
- Add typed runtime payloads, client methods, service methods, and controller endpoints for the three collaboration scenarios.
- Reuse existing apply flows where possible:
  - clarification collaboration -> apply selected clarification suggestions as `ClarificationItem`
  - decision-budget collaboration -> apply recommendation and budget note onto `DecisionItem`
  - product-architecture brief -> save as `MeetingRecord`

## Frontend
- Requirement Intake: add a collaboration preview entry for Requirement Analyst + Product Manager.
- Decision Center: add a collaboration preview entry for Product Manager + Budget Analyst.
- Project Center: add a collaboration preview entry for Product Manager + Architect.
- Keep all actions operator-confirmed with modal/drawer previews; no automatic writes.

## Validation
- Run runtime unit tests, backend compile plus targeted runtime/service tests, and frontend `npm run tsc`.
- Document the module boundary and current limitations in `docs/modules/lightweight-collaboration.md`.
