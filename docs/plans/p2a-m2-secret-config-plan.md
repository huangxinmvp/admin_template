# P2A-M2 Secret and Config Hygiene Plan

## Goal

P2A-M2 improves security hygiene for runtime and external integration credentials without introducing a full vault or secret-manager system.

The goal is not to redesign the platform's config model. The goal is to make the current model safer and easier to operate in a controlled pilot environment.

## Why This Milestone Now

After P2A-M1, the environment and config model is documented clearly enough to operate. The next risk is not where values live conceptually, but how sensitive values are handled in practice.

Current repository reality:

- environment override already takes precedence over database system-config
- password-style config is already masked in UI
- runtime and external integrations are already verified for controlled real-environment demo use
- database-backed system-config is still not a secret manager

P2A-M2 should reduce operator error and trial-time exposure risk before P2A-M3 browser regression work.

## Scope

P2A-M2 focuses on:

- environment-variable-first handling where reasonable
- preventing secret values from being logged
- preserving masked UI behavior
- documenting the limits of database-backed secret storage
- adding a practical credential rotation and expiry runbook

P2A-M2 does not include:

- vault integration
- KMS integration
- a new secret-management product feature
- tenant-level secret partitioning redesign
- deployment platform redesign

## Current Facts To Anchor On

### Config precedence

Current backend precedence is:

1. environment override
2. database system-config
3. code default

Source:

- [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)

### UI masking behavior

Password-style values:

- are rendered as password fields in config center
- are not re-displayed after load
- preserve the current stored value when the operator saves a blank value

Source:

- [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)
- [index.tsx](/Users/hx/it_company/agent_company/web_frontend/src/pages/system/config-center/index.tsx)

### Runtime and integration credentials

Current sensitive keys include:

- `agentRuntime.apiKey`
- `integration.linear.apiKey`
- `integration.figma.apiKey`
- runtime provider keys held only in `agent_runtime` environment variables

### Logging hygiene

Current outbound integration clients log:

- request IDs
- status codes
- failure bodies from providers when needed for diagnostics

Current code does not log:

- configured API keys
- secret header values
- password-style system-config values

This is good enough for pilot hygiene, but should still be documented explicitly so future work does not regress the boundary.

## Planned Deliverables

### 1. Milestone plan

[p2a-m2-secret-config-plan.md](/Users/hx/it_company/agent_company/docs/plans/p2a-m2-secret-config-plan.md)

Purpose:

- define the hygiene scope precisely
- prevent vault-scale expectations from creeping into this milestone

### 2. Rotation runbook

[credential-rotation-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/credential-rotation-runbook.md)

Purpose:

- provide practical steps for rotating:
  - Spring Boot to runtime shared key
  - Linear API key
  - Figma API key
  - runtime provider credentials
- document verification steps after each rotation
- document the fallback/rollback steps

### 3. Module doc

[config-security-hygiene.md](/Users/hx/it_company/agent_company/docs/modules/config-security-hygiene.md)

Purpose:

- record current intended handling model
- document the environment-variable-first rule
- document UI masking behavior
- document the current storage limitations and production boundary

## Acceptance Criteria

P2A-M2 is complete when:

- the preferred placement of each credential type is explicit
- the UI masking and blank-save semantics are documented clearly
- logging guidance states what must not be logged
- current storage limits are stated plainly
- a practical rotation runbook exists for pilot operators
- the docs do not imply a vault-grade capability that the repo does not yet have

## Validation Plan

Validation for this milestone is implementation-aligned documentation validation:

- confirm precedence and masking behavior against [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)
- confirm config-center behavior against [index.tsx](/Users/hx/it_company/agent_company/web_frontend/src/pages/system/config-center/index.tsx)
- confirm runtime credential handling against [config.py](/Users/hx/it_company/agent_company/agent_runtime/config.py)
- confirm external clients do not log secret header values in current code paths
- confirm runbook guidance remains consistent with existing local/demo/pilot environment docs

## Expected Follow-On Gaps Before P2A-M3

After P2A-M2, the main remaining gaps before P2A-M3 will be:

- no browser-level regression automation yet
- no screenshot automation support yet
- no broader E2E baseline across role-based pilot flows yet
- no stronger secret-at-rest controls beyond the current masked UI and operator discipline
- no formal secret expiry alerts or rotation automation
