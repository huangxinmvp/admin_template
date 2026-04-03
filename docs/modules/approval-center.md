# Approval Center Module

## Purpose

M7 establishes the formal approval layer for AICoOS.

Instead of leaving high-impact decisions and budget-sensitive changes as isolated business records, Approval Center turns them into explicit approval items with visible status, operator history, and project-level governance context.

## Backend Scope

Approval Center is implemented inside the existing Phase 1 backend module and extends the existing `ApprovalRecord` model instead of introducing a new workflow engine.

### Core Records

- `ApprovalRecord`
  - formal approval object for high-impact governance actions
- `ApprovalActionLog`
  - append-only history for approval actions and linkage creation

### Extended Approval Fields

`ApprovalRecord` now carries the operator-facing fields needed by M7:

- `title`
- `projectId`
- `approvalType`
- `sourceObjectType`
- `sourceObjectId`
- `decisionItemId`
- `requesterUserId`
- `approverUserId`
- `description`
- `riskSummary`
- `budgetImpactSummary`
- `recommendedAction`
- `blockerFlag`
- `approvalStatus`
- `operatorUserId`
- audit timestamps from the existing template

### Approval Actions

Custom endpoints were added under `/api/aicoos/approvalRecord`:

- `GET /api/aicoos/approvalRecord/{id}/actions`
  - returns approval action history
- `POST /api/aicoos/approvalRecord/{id}/actions`
  - applies `approve`, `reject`, `request_changes`, or `defer`
- `GET /api/aicoos/approvalRecord/source`
  - returns approvals linked to a source object

Action history is persisted in `ai_approval_action_log` with:

- previous status
- next status
- action type
- action comment
- operator user id
- operated time

### Lightweight Linkage

M7 keeps linkage explicit and operator-driven.

Custom endpoints:

- `POST /api/aicoos/approvalRecord/decisionItem/{decisionItemId}/link`
- `POST /api/aicoos/approvalRecord/budgetPlan/{budgetPlanId}/link`

Behavior:

- creates or reopens a linked approval for a decision or budget plan
- copies core context into the approval record
- records a `created` action log entry

No approval rule builder, notification automation, or multi-step approval orchestration is introduced in M7.

## Frontend Scope

### Route

`/aicoos/approval-center` is now the dedicated Approval Center page.

The old generic approval resource route redirects here so the operator flow stays centered on governance rather than raw CRUD.

### Main UI Parts

- approval list table
  - title
  - linked project
  - approval type
  - source object
  - requester
  - recommended action
  - blocker flag
  - current status
  - last updated info
- create / edit modal
- approval detail drawer
  - project and source context
  - description, risk, and budget summaries
  - requester / approver / operator context
  - action history timeline
- action modal
  - approve
  - reject
  - request changes
  - defer
  - optional comment

## Decision And Budget Integration

Decision Center and Budget Center now support lightweight approval linkage:

- decisions can create or open a linked approval
- budget plans can create or open a linked approval
- linked approval state is shown from decision and budget detail drawers
- operators can jump directly into Approval Center from related records

## Project Center Integration

Project Center now surfaces:

- pending approval count
- blocker approval count
- recent approvals in project detail
- quick entry into Approval Center from the project drawer

## Current Limits

M7 intentionally stays in the governance-console layer.

Out of scope:

- multi-step approval workflow design
- notification, email, IM, or SLA escalation
- automatic budget recalculation
- automatic stage transitions
- complex approval routing or rule configuration
