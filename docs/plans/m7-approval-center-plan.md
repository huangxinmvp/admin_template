# M7 Approval Center Plan

## Scope

Build a dedicated Approval Center on top of the existing `ApprovalRecord` skeleton so high-impact project, decision, and budget actions can move through an explicit approval layer with status, history, and project visibility.

## Backend

- extend `ApprovalRecord` with business-facing approval fields:
  - title
  - source object type / id
  - requester
  - description / background
  - risk summary
  - budget impact summary
  - recommended action
  - blocker flag
  - operator info
- add explicit approval action history with:
  - action type
  - previous status
  - next status
  - operator
  - comment
  - operated time
- add lightweight Approval Center aggregate endpoints for:
  - approval list
  - approval detail
  - approval action history
  - approval actions
  - linked approvals by source object
- keep decision and budget linkage explicit:
  - manual create-or-open approval from `DecisionItem`
  - manual create-or-open approval from `BudgetPlan`

## Frontend

- replace the generic approval route with a dedicated Approval Center page
- build:
  - approval list with filters
  - approval detail drawer
  - action modal for approve / reject / request_changes / defer
  - action history timeline
- add lightweight linked-approval surfaces into:
  - Decision Center
  - Budget Center
  - Project Center

## Limits

- no workflow engine
- no notifications or escalation
- no approval rule builder
- no automatic budget recalculation
- no automatic stage transitions

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`
- document the module in `docs/modules/approval-center.md`
