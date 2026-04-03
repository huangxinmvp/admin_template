# Figma Integration

## Purpose
M13 adds lightweight Figma context linkage so projects can carry explicit design context without introducing design writeback automation.

This milestone is read/linkage first:
- read Figma file or node context
- preview metadata in AICoOS
- explicitly attach that context to the project

## Supported Flow
From Project Center, operators can:
- paste a Figma URL
- or provide `fileKey` and optional `nodeId`
- preview resolved file/node metadata
- confirm the linkage into project tool bindings

Supported binding types:
- `figma_file`
- `figma_node`

## Current Scope
M13 supports:
- reading Figma file metadata
- reading optional node metadata
- storing linked design context on the project
- surfacing bindings and recent integration actions in Project Center

M13 does not support:
- Figma writeback
- commenting in Figma
- generating designs
- synchronizing design changes back into workflow state

## Config
Configured through existing system config entries:
- `integration.figma.enabled`
- `integration.figma.apiBaseUrl`
- `integration.figma.apiKey`

Default API base URL:
- `https://api.figma.com/v1`

Notes:
- `integration.figma.apiKey` uses the current system-config storage path.
- It is masked in UI but not yet backed by a dedicated secret-management solution.
- Real-environment verification steps are documented in [figma-integration-verification.md](/Users/hx/it_company/agent_company/docs/runbooks/figma-integration-verification.md).

## Safety Boundaries
- Operator initiated only
- Preview before confirm
- No autonomous project updates
- No automatic stage transitions from design context reads
- No background polling or sync

## Auditability
Figma preview/apply actions are written into `ToolIntegrationAudit` and shown in:
- project detail -> recent tool integration actions
- project recent activity timeline

This keeps external-context reads visible to PMO/operators without turning Figma into a hidden side channel.

## M13.5 Hardening Notes
- UI now states clearly that Figma flow is read/link only
- node lookup failures now surface an explicit actionable message instead of silently degrading to file-only context
- auth/config errors are mapped to config-key-oriented operator guidance for easier verification
