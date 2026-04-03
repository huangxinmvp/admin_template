# Clarification Center Module

## Purpose

M4 adds the first project-scoped clarification workflow for AICoOS.

The goal is to turn missing or ambiguous requirement information into explicit, trackable clarification items that can be reviewed from both the dedicated Clarification Center and Project Center.

## Backend Scope

Clarification Center is implemented inside the existing Phase 1 backend module:

- entity
  - `ClarificationItem`
- table
  - `ai_clarification_item`
- controller
  - `/api/aicoos/clarificationItem`
- custom helper endpoint
  - `POST /api/aicoos/clarificationItem/project/{projectId}/mock-generate`

### Main Fields

- `projectId`
- `title`
- `question`
- `category`
- `severity`
- `suggestedOptions`
- `userResponse`
- `status`
- `generatedFlag`
- `remark`

### Enum Semantics

- category
  - `business_goal`
  - `feature_scope`
  - `reference_benchmark`
  - `timeline`
  - `budget`
  - `technical_constraint`
  - `integration`
  - `acceptance`
- severity
  - `low`
  - `medium`
  - `high`
  - `blocker`
- status
  - `open`
  - `awaiting_response`
  - `answered`
  - `resolved`

### Mock Generation

M4 does not call an agent runtime to generate clarification items.

Instead, the mock-generate endpoint creates a small set of typed starter questions using the current intake gaps:

- business goal missing
- feature scope missing
- timeline missing
- budget range missing
- technical constraints missing
- reference examples missing

If the intake is already reasonably complete, the helper falls back to generic acceptance and dependency questions.

## Frontend Scope

### Route

- `/aicoos/clarification-center`

### Page Behavior

Clarification Center is a dedicated project-centric page with:

- project selector
- clarification list table
- filters for category, severity, and status
- create/edit modal
- detail drawer
- mock-generate action
- quick links back to Project Center and Requirement Intake

## Project Center Integration

Project Center now surfaces clarification-driven governance signals:

- requirement completeness
- clarification count
- blocker clarification count
- recent clarification items
- intake and clarification entry points from the project list and project detail drawer

## Current Limits

- no real model-based clarification generation yet
- no approval workflow is triggered automatically from a clarification item
- no email or calendar follow-up
- no full stage transition logic tied to clarification resolution
