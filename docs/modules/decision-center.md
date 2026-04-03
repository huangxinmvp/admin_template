# Decision Center Module

## Purpose

M5 turns the existing `DecisionItem` skeleton into AICoOS's formal confirmation layer.

The goal is to make important requirement outcomes, governance uncertainties, and promoted clarification issues reviewable through explicit decision records and explicit operator actions.

## Backend Scope

Decision Center stays inside the existing Phase 1 backend module.

### Core Records

- `DecisionItem`
  - formal confirmation record
- `DecisionActionLog`
  - append-only action history for confirm / reject / defer and promotion events

### Extended Decision Fields

`DecisionItem` now carries the fields needed for operator-facing confirmation:

- `title`
- `projectId`
- `itemType`
- `sourceType`
- `sourceId`
- `description`
- `impactSummary`
- `suggestedOptions`
- `recommendedOption`
- `budgetImpactSummary`
- `projectImpactSummary`
- `blockerFlag`
- `dueAt`
- `status`
- audit fields from the existing template

### Decision Actions

Custom endpoints were added under `/api/aicoos/decisionItem`:

- `GET /api/aicoos/decisionItem/{id}/actions`
  - returns decision action history
- `POST /api/aicoos/decisionItem/{id}/actions`
  - applies confirm / reject / defer with optional comment

Action history is persisted in `ai_decision_action_log` with:

- previous status
- next status
- action type
- action comment
- operator user id
- operated time

### Clarification Promotion

M5 keeps clarification promotion manual and explicit.

Custom endpoint:

- `POST /api/aicoos/clarificationItem/{id}/promote`

Behavior:

- creates a decision prefilled from the clarification item
- links the clarification record back to the promoted decision
- writes a promotion action log entry

No automatic model-driven promotion or approval workflow is introduced in M5.

## Frontend Scope

### Route

- `/aicoos/decision-center`

The old generic decision resource route now redirects here.

### Main UI Parts

- decision list table
  - title
  - linked project
  - type
  - source
  - blocker flag
  - recommended option
  - due date
  - last updated info
- create / edit modal
- decision detail drawer
  - full decision description
  - impact summaries
  - suggested and recommended options
  - blocker and due-date context
  - action history timeline
- action modal
  - confirm
  - reject
  - defer
  - optional comment

### Clarification Center Integration

Clarification Center now supports manual promotion of a clarification item into a formal decision.

Operators can:

- promote a clarification item
- jump to Decision Center for the same project

## Project Center Integration

Project Center now surfaces:

- pending decision count
- blocker decision count
- recent decisions in project detail
- quick links into Decision Center from project-level views

## Current Limits

- no email, notification, or calendar automation
- no full approval workflow on top of decisions
- no budget recalculation side effects
- no automatic stage transitions triggered by decision status
- no AI-generated promotion rules beyond manual operator choice
