# P2A-M5 Pilot Deployment Plan

## Goal

P2A-M5 packages the current AICoOS pilot-ready system into a clearly deployable, verifiable, and recoverable operating package for controlled pilot environments.

This milestone is operational and documentation focused. It does not add new product features.

## Why This Milestone Now

After P2A-M4, the repo already has:

- environment/config governance guidance
- secret/config hygiene guidance
- repeatable browser-level regression support
- lightweight diagnostics and troubleshooting guidance

The next missing piece is pilot handoff readiness:

- what exactly must be deployed
- what must be configured
- what must be verified before go-live
- how to upgrade safely
- how to roll back safely
- what the pilot boundary actually is

## Scope

P2A-M5 covers:

- pilot deployment checklist
- upgrade runbook
- rollback runbook
- pilot environment boundary document
- plan document tying those materials together

P2A-M5 does not cover:

- new product features
- deployment automation platform work
- HA architecture implementation
- infrastructure-as-code rollout
- production signoff

## Deployment Package Components

The current pilot package is built from:

- Spring Boot backend
- frontend
- `agent_runtime`
- MySQL / Flowable-compatible database
- system-config and environment overrides
- verified Linear and Figma integration paths

## Deliverables

### 1. Milestone plan

[p2a-m5-pilot-deployment-plan.md](/Users/hx/it_company/agent_company/docs/plans/p2a-m5-pilot-deployment-plan.md)

Purpose:

- define the deployment-handoff scope for this milestone

### 2. Pilot deployment checklist

[pilot-deployment-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-checklist.md)

Purpose:

- define what must exist and what must be verified before pilot use

### 3. Upgrade runbook

[upgrade-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/upgrade-runbook.md)

Purpose:

- define pre-upgrade checks, backups, bootstrap/migration considerations, and post-upgrade verification

### 4. Rollback runbook

[rollback-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/rollback-runbook.md)

Purpose:

- define rollback triggers, rollback procedure, and post-rollback verification

### 5. Pilot boundary document

[pilot-environment-boundaries.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-environment-boundaries.md)

Purpose:

- state clearly what the pilot version supports and does not support

## Acceptance Criteria

P2A-M5 is complete when:

- a pilot operator can identify all required deployable components
- startup order and health checks are explicit
- upgrade and rollback procedures are documented in operational order
- integration/config cautions are explicit during upgrade and rollback
- the pilot boundary is clear enough to avoid accidental production claims

## Validation Plan

Validation for this milestone is documentation consistency validation:

- align deployment checklist with current environment/config guide
- align verification steps with current browser regression and diagnostics docs
- align integration readiness with current Linear/Figma verification state
- align production boundary wording with current production gap statements

## Remaining Gaps After P2A-M5

Even after this milestone, the system should still not claim:

- production hardening completeness
- HA readiness
- DR readiness
- full observability maturity
- autonomous multi-agent operation

Those remain future concerns outside the P2A pilot-readiness package.
