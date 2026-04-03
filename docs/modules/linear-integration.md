# Linear Integration

## Purpose
M13 adds a controlled Linear integration so operators can project AICoOS governance objects into Linear without enabling autonomous external writes.

The integration is intentionally preview-first:
- preview before write
- operator confirms every external write
- writes are audited back into AICoOS
- AICoOS remains the source of truth

## Supported Flows

### 1. Project representation
From Project Center, operators can:
- create a primary Linear issue for the project
- or link an existing Linear issue as the project's primary external representation

This primary issue becomes the default target for later clarification/decision comments.

### 2. Clarification export
From Clarification Center, operators can preview and then:
- create a new Linear issue from a clarification item
- or append a clarification comment to the project's primary Linear issue

### 3. Decision export
From Decision Center, operators can preview and then:
- create a new Linear issue from a decision item
- or append a decision comment to the project's primary Linear issue

## Backend Boundary
Backend entrypoint:
- `/api/aicoos/toolIntegration/...`

Key backend types:
- `ProjectToolBinding`
- `ToolIntegrationAudit`

Current binding types:
- `linear_primary_issue`

Current audit action types:
- `linear_project_preview`
- `linear_project_apply`
- `linear_issue_preview`
- `linear_issue_apply`
- `linear_comment_preview`
- `linear_comment_apply`

## Config
Configured through existing system config entries:
- `integration.linear.enabled`
- `integration.linear.graphqlUrl`
- `integration.linear.apiKey`
- `integration.linear.defaultTeamId`

Notes:
- `integration.linear.apiKey` is stored using the current system-config mechanism.
- It is masked in the config UI, but it is not yet backed by a dedicated secret manager.
- Real-environment verification steps are documented in [linear-integration-verification.md](/Users/hx/it_company/agent_company/docs/runbooks/linear-integration-verification.md).

## Safety Boundaries
- No autonomous writes
- No background sync
- No bidirectional sync
- No automatic comment/issue creation from runtime suggestions
- No workflow or approval auto-advancement after external writes

## Auditability
Every preview/apply operation records audit data under:
- project detail -> recent tool integration actions
- project recent activity timeline

Audit records capture:
- tool type
- action type
- source object type/id
- preview vs confirmed write
- operator
- time
- external object link when available
- failure message when the write fails

## M13.5 Hardening Notes
- UI now shows explicit preview/apply guidance in the operator modal
- missing project mapping errors are actionable for Comment mode
- auth/config errors are mapped to config-key-oriented messages for easier real-environment verification
