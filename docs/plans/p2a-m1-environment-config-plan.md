# P2A-M1 Environment and Config Governance Plan

## Goal

P2A-M1 establishes a clean, reproducible environment and configuration foundation for:

- Spring Boot backend
- frontend
- `agent_runtime`
- Linear integration
- Figma integration

This milestone is documentation- and runbook-first. It should reduce ambiguity before any broader pilot hardening work.

## Why This Milestone First

Current AICoOS behavior is already strong enough to demo, but pilot operation still depends on knowledge spread across:

- startup scripts
- environment variables
- system-config keys
- demo runbooks
- external verification notes

Without a consolidated environment/config foundation, later pilot work would continue to inherit:

- unclear secret ownership
- inconsistent startup assumptions
- environment drift between local and pilot usage
- operator confusion around which values are safe to change in UI

## Scope

P2A-M1 covers:

- environment/config documentation cleanup
- startup order documentation
- explicit config precedence rules
- separation notes for local, demo, and pilot environments
- integration config documentation for runtime, Linear, and Figma

P2A-M1 does not cover:

- new product features
- new deployment infrastructure
- browser automation implementation
- observability stack rollout
- Phase 2B work

## Current Facts To Anchor On

### Backend

- Spring Boot reads DB connectivity from `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`
- [start_backend_local.sh](/Users/hx/it_company/agent_company/scripts/start_backend_local.sh) sets local MySQL defaults around `127.0.0.1:3306/admin_template`

### Runtime

- `agent_runtime` runs separately from Spring Boot
- [start_agent_runtime_mock.sh](/Users/hx/it_company/agent_company/scripts/start_agent_runtime_mock.sh) starts the fastest local path
- runtime configuration is environment-variable based

### Frontend

- local dev uses Umi proxy settings
- [proxy.ts](/Users/hx/it_company/agent_company/web_frontend/config/proxy.ts) points `/api/` traffic to `http://127.0.0.1:8081`
- that proxy is development-only and should not be treated as a pilot deployment mechanism

### Config precedence

Current backend system-config precedence is:

1. environment override
2. database system-config
3. code default

This rule already exists in implementation and should become a first-class operational rule in pilot docs.

### External integrations

- Linear is verified for controlled real-environment demo usage
- Figma is verified for controlled real-environment demo usage
- both still rely on the current system-config plus environment-override model

## Planned Deliverables

### 1. Phase guide

[phase-2a-pilot-readiness.md](/Users/hx/it_company/agent_company/docs/plans/phase-2a-pilot-readiness.md)

Purpose:

- define why Phase 2A exists
- place P2A-M1 in the broader pilot-readiness sequence
- keep the phase boundary clear

### 2. Milestone plan

[p2a-m1-environment-config-plan.md](/Users/hx/it_company/agent_company/docs/plans/p2a-m1-environment-config-plan.md)

Purpose:

- scope the milestone tightly
- record objectives, acceptance criteria, and handoff expectations

### 3. Operator runbook

[environment-config-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/environment-config-guide.md)

Purpose:

- consolidate startup order
- document environment classes
- document config ownership
- document integration keys and precedence
- reduce config/setup drift

## Acceptance Criteria

P2A-M1 is complete when:

- startup order is documented clearly for local/demo/pilot contexts
- config precedence is written once and used consistently
- the local/demo/pilot distinction is explicit
- runtime, Linear, and Figma config keys are documented in one place
- secret placement guidance is explicit
- the runbook warns clearly that pilot-ready is not production-ready

## Validation Plan

Validation for this milestone is documentation consistency validation:

- confirm script references match current repo files
- confirm config precedence matches [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)
- confirm integration guidance matches current verified Linear/Figma state
- confirm runbook recommendations match existing local startup scripts and proxy behavior

## Expected Follow-On Gaps For P2A-M2

After P2A-M1, the main remaining gaps before P2A-M2 will be:

- no stronger secret-management model yet
- no explicit rotation or expiry handling runbook for pilot credentials
- no browser-level regression support yet
- no broader observability stack yet
- no deployment manifest standard yet
