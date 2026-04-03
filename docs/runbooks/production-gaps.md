# Known Limitations And Production Gaps

## Purpose

This document records the known limits of the current AICoOS demo baseline and the main reasons it should still be treated as a controlled demo handoff rather than a production-ready platform.

## Current Positioning

The current repo is suitable for:

- guided local demos
- stakeholder walkthroughs
- architecture review
- implementation handoff
- phase-planning for the next milestone

The current repo is not yet suitable for:

- unattended production operation
- autonomous multi-agent delivery execution
- enterprise-grade runtime operations
- broad tenant self-service rollout

## Product Gaps

### Governance is visible more often than it is enforced

- workflow stages, gates, risks, approvals, and budgets are modeled and surfaced
- hard enforcement is still incomplete across transitions and downstream consequences
- release-critical controls are not yet backed by a full execution policy layer

### AI is suggestion-first by design

- `agent_runtime` returns structured suggestions and previews
- there is no autonomous task execution loop
- there is no PR, deployment, or code-delivery automation in this baseline
- collaboration flows are curated demo scenarios, not general orchestration

### External integrations are intentionally partial

- Linear uses preview/apply flows
- Figma uses read-and-bind flows
- there is no bidirectional sync
- there is no background polling, reconciliation, or automatic workflow advancement

## Operational Gaps

### Local startup is still operator-driven

- the demo depends on local startup order for MySQL, backend, frontend, and `agent_runtime`
- the repo now includes a single-host pilot Compose baseline, but not a production orchestration layer
- runtime scaling, failover, and HA topology are not part of this baseline

### Secrets and configuration are not production-grade

- config values can be masked in UI and overridden by environment variables
- the current system-config path is still not a proper secret-management solution
- vault integration, secret rotation, and environment promotion discipline are still follow-up work

### Observability is still lightweight

- request correlation IDs exist for runtime and integration calls
- there is no full tracing stack
- there is no metrics dashboard or alerting setup
- there is no synthetic monitoring or incident-response workflow

### Reliability controls are incomplete

- runtime and tool calls have timeouts and lightweight retries
- there is no durable queue, replay mechanism, or retry-budget strategy
- there is no broader circuit-breaker or bulkhead design yet

## Delivery And Repo Gaps

### Validation baseline still needs tightening

- full backend and frontend validation remains environment-dependent
- browser-level end-to-end verification is still largely runbook-driven
- demo readiness still depends on operator checks instead of a clean automated release gate

### The repository baseline is not yet fully packaged as a clean release snapshot

- the worktree already contains substantial unrelated code changes outside this documentation closeout
- `git status` also shows a large removed `camunda-modeler` subtree and other in-progress modifications
- controlled handoff should therefore use a reviewed branch or commit, not an arbitrary local working tree snapshot

### Deployment readiness is incomplete

- there is now a pilot deployment manifest for the full stack under `deploy/pilot/compose.yaml`
- rollout, rollback, DR, and environment promotion runbooks are not complete
- production infrastructure ownership boundaries are not yet documented end to end

## Recommended Handoff Framing

Describe this repo as:

- a credible Phase 1 governance demo
- a reusable console and business-core foundation
- a preview-first AI assistance model with explicit operator control
- a strong candidate for Phase 2 stabilization and operational hardening

Do not describe this repo as:

- production-ready
- autonomous
- fully validated
- enterprise-hardened

## Companion Docs

- [aicos-demo-v0.1.md](/Users/hx/it_company/agent_company/docs/release-notes/aicos-demo-v0.1.md)
- [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
- [production-hardening.md](/Users/hx/it_company/agent_company/docs/modules/production-hardening.md)
- [phase-2-roadmap.md](/Users/hx/it_company/agent_company/docs/plans/phase-2-roadmap.md)
