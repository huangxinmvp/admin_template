# P2B-M2 Collaboration Governance Linkage Plan

## Scope
- Keep the existing three fixed collaboration scenarios unchanged:
  - requirement clarification review
  - decision + budget review
  - product + architecture brief
- Strengthen how collaboration results connect to current governance surfaces without introducing autonomous execution, automatic approvals, budget recalculation, or stage transitions.

## Implementation
- Backend:
  - extend collaboration preview linkage with clearer project-governance context derived from existing project state
  - add a lightweight `ProjectCollaborationSummary` view model derived from existing clarification, decision, and meeting records only
  - keep apply/save targets unchanged:
    - clarification collaboration -> `ClarificationItem`
    - decision-budget collaboration -> `DecisionItem`
    - product-architecture collaboration -> `MeetingRecord`
- Frontend:
  - add a Project Center “协作治理联动” section showing collaboration-derived governance signals
  - tighten collaboration preview modals so operators can see how results relate to clarification, decision, budget, and governance views before applying

## Validation
- Run backend compile validation for the Spring Boot changes.
- Run frontend `npm run tsc`.
- Manually verify the Project Center detail view still loads and collaboration preview/apply paths remain suggestion-first.
