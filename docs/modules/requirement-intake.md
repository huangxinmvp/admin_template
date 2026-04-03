# Requirement Intake Module

## Purpose

M4 adds the first structured requirement intake workflow for AICoOS.

The goal is to turn a vague project request into a saved intake record that can be reviewed, clarified, and surfaced inside Project Center without introducing a separate agent runtime.

## Backend Scope

Requirement Intake is implemented inside the existing Phase 1 backend module:

- entity
  - `RequirementIntake`
- table
  - `ai_requirement_intake`
- controller
  - `/api/aicoos/requirementIntake`
- custom project-linked endpoints
  - `GET /api/aicoos/requirementIntake/project/{projectId}`
  - `PUT /api/aicoos/requirementIntake/project/{projectId}`

### Main Fields

- `projectId`
- `projectName`
- `projectType`
- `businessGoal`
- `featureSummary`
- `referenceProducts`
- `timelineExpectation`
- `budgetRange`
- `technicalConstraints`
- `notes`
- `attachmentPlaceholders`

### Project Linkage

Requirement Intake is one record per project.

When a project-linked intake is saved:

- the intake record is created or updated by `projectId`
- `Project.projectName` is synchronized
- `Project.projectType` is synchronized
- `Project.intakeSummary` is refreshed from the latest intake content

This keeps Project Center aligned with the latest requirement intake without adding workflow automation in M4.

## Frontend Scope

### Route

- `/aicoos/requirement-intake`

### Page Behavior

The intake page is a dedicated form-based admin page, not a generic CRUD table.

It supports:

- selecting a project
- loading an existing intake by project
- editing the structured intake fields
- saving the intake back through the project-linked endpoint
- jumping directly to Clarification Center for the same project

### Current Limits

- attachments are placeholders only
- intake does not create a project from scratch in M4
- no AI generation happens here yet
- no automatic project-stage transition is triggered by intake save
