# Agent Role Management

## Scope

M9 establishes the organizational layer for AICoOS by making AI agents explicit business roles instead of hidden automation settings.

This milestone adds:

- organization-level Agent role records
- stage participation configuration
- allowed action configuration
- Project Center role visibility

This milestone does not add:

- agent runtime orchestration
- autonomous multi-role collaboration
- model routing
- complex permission DSL
- per-project runtime task dispatch

## Backend Structure

The backend keeps `AgentRole` as the root organizational record and extends it with governance-oriented fields:

- role code
- role name
- role type
- description
- responsibility summary
- status
- default flag
- budget factor
- approval-collaboration flag

Two lightweight child configs support M9:

### AgentRoleStageParticipation

Defines whether a role participates in a lifecycle stage:

- required
- optional
- not involved

### AgentRoleAllowedAction

Defines whether a role may perform a lightweight governed action, such as:

- create or update clarification items
- create decisions
- propose budget changes
- request approvals
- update stage suggestions
- prepare release items
- perform high-risk actions only with approval

## Frontend Structure

`/aicoos/agent-roles` is now a dedicated management center instead of the generic CRUD page.

The page includes:

- role list
- role detail drawer
- create/edit modal
- stage participation config
- allowed action config

The UI reuses the existing admin template patterns:

- `PageContainer`
- `ProTable`
- `ModalForm`
- drawer-based detail view

## Project Center Integration

Project Center now derives lightweight organizational guidance from enabled roles:

- linked Agent roles
- current-stage recommended roles
- missing critical roles when the current stage has no required enabled roles

This is derived from:

- the project current stage
- workflow-template stage coverage
- role stage participation rules
- allowed action configuration

## Current Limitations

M9 is intentionally governance-only.

It does not yet support:

- runtime role assignment
- workload balancing
- autonomous collaboration loops
- agent execution permissions against tools
- stage auto-dispatch

Those capabilities should build on top of this organizational layer rather than bypassing it.
