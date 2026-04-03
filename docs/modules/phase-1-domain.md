# Phase 1 Domain Module Structure

## Scope

This document records the M2 Phase 1 business-domain skeleton added on top of the existing Spring Boot + Ant Design admin template.

The goal of this slice is to establish typed governance modules and admin-console scaffolding for AICoOS without introducing agent runtime execution, orchestration services, or heavy new infrastructure.

## Backend Module Layout

Backend code is organized under `web_backend/src/main/java/com/hiking/treasure/modules/phase1/`.

### Package Structure

- `controller`
  - `AbstractPhase1CrudController`
  - `ProjectController`
  - `ProjectStageController`
  - `DecisionItemController`
  - `ApprovalRecordController`
  - `BudgetPlanController`
  - `BudgetLedgerController`
  - `AgentRoleController`
  - `WorkflowTemplateController`
- `domain/convert`
  - MapStruct converters for each domain object
- `domain/dto/create`
  - create DTOs for each domain object
- `domain/dto/update`
  - update DTOs for each domain object
- `domain/dto/query`
  - query DTOs for each domain object
- `domain/vo`
  - list/detail response VOs for each domain object
- `entity`
  - MyBatis Plus entities mapped to Phase 1 tables
- `enums`
  - `Phase1DomainEnums` with stage, status, type, risk, and ledger enums
- `mapper`
  - MyBatis Plus mappers
- `service`
  - service interfaces
- `service/impl`
  - service implementations

### Domain Objects

- `Project`
  - Core project governance record
- `ProjectStage`
  - Stage and gate-tracking node under a project
- `DecisionItem`
  - Pending clarification, scope, budget, or release decision
- `ApprovalRecord`
  - Approval history linked to project and decision context
- `BudgetPlan`
  - Project budget plan and approval state
- `BudgetLedger`
  - Budget movement ledger for reserve/consume/adjust/refund actions
- `AgentRole`
  - Governance or delivery-facing agent role definition
- `WorkflowTemplate`
  - Stage-template shell for governed project execution

### Database Tables

Bootstrap SQL is added in `web_backend/src/main/resources/static/sql/bootstrap-aicoos-phase1-domain.sql`.

Tables:

- `ai_project`
- `ai_project_stage`
- `ai_decision_item`
- `ai_approval_record`
- `ai_budget_plan`
- `ai_budget_ledger`
- `ai_agent_role`
- `ai_workflow_template`

Each table follows the existing template conventions:

- string ID with MyBatis Plus assigned ID
- `tenant_id`
- `del_flag`
- `create_by`, `create_time`
- `update_by`, `update_time`

### API Endpoints

All Phase 1 endpoints live under `/api/aicoos/*`.

- `/api/aicoos/project`
- `/api/aicoos/projectStage`
- `/api/aicoos/decisionItem`
- `/api/aicoos/approvalRecord`
- `/api/aicoos/budgetPlan`
- `/api/aicoos/budgetLedger`
- `/api/aicoos/agentRole`
- `/api/aicoos/workflowTemplate`

Each controller currently exposes a consistent minimal CRUD surface:

- `GET /page`
- `GET /{id}`
- `POST /`
- `PUT /{id}`
- `DELETE /{id}`
- `DELETE ?ids=...`

### Default Enum Semantics

The current skeleton uses enum-backed defaults for coherent first records:

- project defaults
  - type: `delivery`
  - stage: `intake`
  - status: `draft`
  - risk: `medium`
- project stage defaults
  - stage status: `pending`
  - gate status: `pending`
- decision item defaults
  - type: `clarification`
  - priority: `medium`
  - status: `open`
- approval defaults
  - type: `requirement`
  - status: `draft`
- budget defaults
  - plan currency: `TOKEN`
  - plan status: `draft`
  - ledger type: `reserve`
- agent role defaults
  - category: `delivery`
  - status: `active`
  - approval required: `1`
  - max concurrency: `1`
- workflow template defaults
  - version: `1`
  - status: `draft`
  - default flag: `0`

## Frontend Module Layout

Phase 1 console scaffolding reuses the existing generic backend resource page instead of introducing a separate UI framework or feature shell.

### Routes

Frontend routes are added under the new `/aicoos` menu group in `web_frontend/config/routes.ts`.

- `/aicoos/projects`
- `/aicoos/project-stages`
- `/aicoos/decision-items`
- `/aicoos/approval-records`
- `/aicoos/budget-plans`
- `/aicoos/budget-ledgers`
- `/aicoos/agent-roles`
- `/aicoos/workflow-templates`

### Resource Metadata

Resource definitions are registered in `web_frontend/src/features/backend/resourceMeta.ts`.

For each Phase 1 module the metadata defines:

- endpoint path
- table fields
- search fields
- create/edit form fields
- enum-backed select options
- foreign-key option loaders via `queryPageOptions`

### Shared CRUD UX Reuse

The existing `CrudPage` is reused for all Phase 1 routes, with a small shared enhancement:

- list view via `ProTable`
- create/edit via shared modal form
- detail view via shared drawer
- select and relation fields rendered with label values instead of raw IDs where option metadata exists

This keeps the M2 scope aligned with the existing admin template while still providing list/detail/create/edit scaffolding for each module.

## Out of Scope

This module structure intentionally does not implement:

- agent runtime
- MCP orchestration
- long-running task execution
- budget metering side effects
- stage transition rules
- approval workflows beyond CRUD records
- artifact, meeting record, change request, or agent task execution models

Those concerns should land in later milestones once the Phase 1 governance foundation is stable.

## Current Assumptions

- Phase 1 business logic remains inside the existing Spring Boot backend.
- The Ant Design console remains the primary governance UI.
- Demo seed data is not required for M2 because the generic CRUD pages now support manual creation of starter records.
- The new schema is intentionally minimal and may gain indexes, stronger validation, and domain services in later milestones.
