# Project Center Module

## Purpose

M3 turns the Phase 1 `Project` skeleton into a project-centric operations view.

Instead of treating a project as a plain CRUD row, Project Center aggregates the current stage, governance pressure, budget health, and recent activity into one operator-facing view.

## Backend Scope

Project Center is implemented inside the existing Phase 1 module and does not introduce a new service boundary.

### Source Entities

- `Project`
- `ProjectStage`
- `DecisionItem`
- `ApprovalRecord`
- `BudgetPlan`
- `BudgetLedger`
- `WorkflowTemplate`

### Aggregation Endpoints

Project Center extends the existing project controller:

- `GET /api/aicoos/project/center/page`
  - returns project list rows with derived governance and budget fields
- `GET /api/aicoos/project/center/{id}`
  - returns a project detail aggregate

### Derived Data

The current implementation derives lightweight operational signals from existing records:

- current stage
  - resolved from `Project.currentStageCode` and matching `ProjectStage`
- pending decision count
  - derived from open and pending-approval `DecisionItem`
- approval summary
  - derived from `ApprovalRecord`
- budget summary
  - derived from the latest `BudgetPlan`
- budget health status
  - derived from approved versus consumed amount
- recent activity
  - derived from project, stage, decision, approval, and budget timestamps

No new tables are introduced in M3.

## Frontend Scope

### Route

`/aicoos/projects` is now a dedicated Project Center page instead of the generic resource page.

Other Phase 1 modules remain on the shared resource screens.

### Main UI Parts

- project list table
  - project name and code
  - project type
  - current stage
  - risk level
  - budget status
  - pending decision count
  - updated time
- filters and search
  - keyword
  - project type
  - current stage
  - risk level
- project create/edit modal
  - reuses the existing backend CRUD endpoint
- project detail drawer
  - header summary
  - governance summary cards
  - stage overview
  - decision summary
  - approval summary
  - budget summary
  - recent activity timeline

### Shared Template Reuse

Project Center still follows the existing admin-console patterns:

- `PageContainer`
- `ProTable`
- `ModalForm`
- `Drawer`
- `ProCard` and `StatisticCard`
- existing backend request utilities

This keeps M3 aligned with the current template instead of introducing a separate UI framework.

## Current Limits

Project Center is intentionally read-heavy and aggregation-light.

Not included in M3:

- workflow transition automation
- approval actions from the detail view
- budget enforcement
- agent runtime execution
- connector-driven project sync
- a dedicated activity/audit event table

Those concerns should build on top of this module in later milestones instead of being collapsed into M3.
