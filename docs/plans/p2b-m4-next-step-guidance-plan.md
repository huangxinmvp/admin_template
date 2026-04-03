# P2B-M4 Project Next-Step Guidance Plan

## Summary
- Add a read-only project next-step guidance layer on top of existing governance state, collaboration outputs, and current stage context.
- Keep the feature suggestion-first only. It does not auto-transition stages, auto-approve records, or auto-adjust budgets.
- Surface the guidance in two places:
  - compact hint in Project Center list
  - prominent guidance card in Project Center detail

## Implementation Notes
- Backend:
  - add `ProjectNextStepGuidanceVO` and `ProjectNextStepActionVO`
  - extend `ProjectCenterDetailVO` with `nextStepGuidance`
  - extend `ProjectCenterListVO` with `recommendedNextStep`, `recommendedActorRole`, `recommendedPriority`
  - keep guidance derived on read through a dedicated resolver helper
- Guidance priority:
  1. blocker clarifications
  2. blocker decisions
  3. blocker approvals
  4. failed or blocking gates
  5. budget overrun / pending / warning / unplanned
  6. missing critical roles
  7. collaboration-driven follow-up
  8. healthy stage-progress fallback
- Frontend:
  - show list-level next-step hint
  - show detail-level next-step card with reason, blockers, actor, target module, and priority actions
  - deep-link the primary action into the corresponding existing module

## Validation Targets
- Backend resolver tests should cover each priority branch and the healthy fallback.
- Project service tests should confirm:
  - detail returns `nextStepGuidance`
  - list returns compact next-step hint fields
- Frontend should be validated with `npm run tsc` and a manual Project Center route check.

## Assumptions
- Guidance remains derived data and is not persisted into `ProjectGovernanceState`.
- `currentBlockers` contains only hard blockers.
- Collaboration scenarios remain unchanged; this milestone only improves project-level interpretation and visibility.
