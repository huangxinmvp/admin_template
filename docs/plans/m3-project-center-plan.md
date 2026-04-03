# M3 Project Center Plan

## Scope

Build a dedicated Project Center on top of the M2 Phase 1 skeleton so `/aicoos/projects` becomes a project-centric operations view instead of a plain generic CRUD page.

## Backend

- keep `Project`, `ProjectStage`, `DecisionItem`, `ApprovalRecord`, `BudgetPlan`, `BudgetLedger`, and `WorkflowTemplate` as the source entities
- add lightweight typed aggregation DTO/VO models for:
  - project center list row
  - project center detail
  - governance/budget/approval/activity summaries
- add simple project-center endpoints under the existing project controller
  - list page aggregation
  - detail aggregation
- derive temporarily:
  - current stage from `Project.currentStageCode` plus matching `ProjectStage`
  - pending decision count from open/pending `DecisionItem`
  - approval summary from `ApprovalRecord`
  - budget summary/status from latest `BudgetPlan` and recent `BudgetLedger`
  - recent activity from project/stage/decision/approval/budget timestamps

## Frontend

- replace `/aicoos/projects` with a dedicated Project Center page
- keep the rest of the Phase 1 modules on the shared generic resource pages
- build:
  - project list table with filters/search
  - project detail drawer or detail panel
  - summary cards
  - stage overview
  - decision / approval / budget summary blocks
  - recent activity timeline
- reuse existing Ant Design Pro page/table/card/drawer patterns and shared request utilities

## Temporary Data Rules

- do not add full workflow automation
- do not add agent runtime
- do not add new top-level services unless the current phase1 module cannot absorb the logic cleanly
- if timeline data is sparse, derive entries from existing timestamps and entity state changes instead of adding a full activity table in M3

## Validation

- backend: targeted compile
- frontend: targeted type-check/buildable import verification
- document the module outcome in `docs/modules/project-center.md`
