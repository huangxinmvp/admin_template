# M6 Budget Center Plan

## Scope

Build a dedicated Budget Center on top of the existing `BudgetPlan` and `BudgetLedger` records so project budget becomes a first-class governance surface instead of a pair of generic CRUD pages.

## Backend

- keep `BudgetPlan` and `BudgetLedger` as the source records
- extend `BudgetPlan` with lightweight role allocation data for:
  - product / analysis
  - architect
  - ui/ux
  - frontend
  - backend
  - qa
  - devops
  - project coordination
- add lightweight budget-center aggregate endpoints for:
  - project budget overview page
  - project-linked budget detail
- derive per-project summary from the latest budget plan plus recent ledger data:
  - total budget
  - locked budget
  - consumed budget
  - pending increase
  - budget health
  - last updated time
- keep ledger rendering explicit and show linked decision item when `referenceType/referenceId` is present

## Frontend

- replace the generic budget route with a dedicated Budget Center page
- build:
  - project-linked budget overview list
  - budget detail drawer
  - summary cards
  - role allocation breakdown
  - recent ledger/history section
- keep create/edit on top of the existing budget plan and budget ledger CRUD endpoints
- add Project Center entry points into Budget Center

## Limits

- no exact model/token/tool billing
- no automatic recalculation engine
- no approval triggering
- no workflow stage transitions

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`
- document the module in `docs/modules/budget-center.md`
