# Budget Center Module

## Purpose

M6 turns budget into a first-class governance surface for AICoOS.

Instead of treating `BudgetPlan` and `BudgetLedger` as isolated CRUD records, Budget Center aggregates them into a project-linked view that shows budget health, role allocation, and ledger history in the same operator workflow.

## Backend Scope

Budget Center is implemented inside the existing Phase 1 module and reuses the current Spring Boot business backend.

### Source Entities

- `Project`
- `BudgetPlan`
- `BudgetLedger`
- `DecisionItem`

### New Aggregate API

- `GET /api/aicoos/budgetCenter/page`
  - returns project-level budget overview rows
- `GET /api/aicoos/budgetCenter/project/{projectId}`
  - returns the project-linked budget detail aggregate

### Derived Budget Summary

The current implementation derives the following from the latest `BudgetPlan`:

- total budget
  - `approvedAmount` when available, otherwise `proposedAmount`
- locked budget
  - `reservedAmount`
- consumed budget
  - `consumedAmount`
- pending increase
  - `max(proposedAmount - approvedAmount, 0)`
- budget health
  - unplanned / pending / healthy / warning / overrun
- last updated time
  - latest plan `updateTime`, then `effectiveAt`, then `createTime`

### Role-Based Breakdown

`BudgetPlan.roleAllocationsJson` is now the lightweight storage field for role allocation.

Supported role buckets:

- `product_analysis`
- `architect`
- `ui_ux`
- `frontend`
- `backend`
- `qa`
- `devops`
- `project_coordination`

If `roleAllocationsJson` is empty, Budget Center currently falls back to a default derived split so the overview remains usable in M6.

### Ledger Enrichment

Budget ledger rows remain stored in `BudgetLedger`.

Budget Center enriches them with:

- business-facing entry labels
- linked decision title when `referenceType/referenceId` points to a `DecisionItem`

No automatic recalculation engine, approval trigger, or workflow transition logic is introduced in M6.

## Frontend Scope

### Route

`/aicoos/budget-center` is now the dedicated Budget Center page.

The old budget plan and budget ledger resource pages remain in the codebase as hidden maintenance screens, but Budget Center becomes the primary operator surface.

### Main UI Parts

- budget overview table
  - project name
  - project type
  - budget health
  - total budget
  - locked budget
  - consumed budget
  - pending increase
  - last updated time
- project-linked detail drawer
  - header summary
  - budget summary cards
  - latest budget plan summary
  - role allocation breakdown
  - budget ledger history
- lightweight plan and ledger forms
  - reuse existing CRUD endpoints instead of creating a separate budget write service

## Project Center Integration

Project Center now links directly into Budget Center and surfaces the richer budget summary fields:

- total budget
- locked budget
- consumed budget
- pending increase
- recent budget changes with linked decision context when available

## Current Limits

M6 intentionally stays in the governance-console layer.

Out of scope:

- exact model or tool billing
- automatic budget recomputation
- approval automation
- workflow stage transitions
- notification delivery
- agent runtime spending enforcement
