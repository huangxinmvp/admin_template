# P3-M2 Reliability Hardening Plan

## Goal

Improve the reliability and repeatability of the current pilot deployment path without adding product features or starting autonomy work.

P3-M2 should harden the existing Compose-first pilot baseline in four practical areas:

1. stronger secret and config handling baseline
2. clearer runtime and container reliability expectations
3. better deployment repeatability across pilot hosts
4. clearer pilot-operational diagnostics guidance

## Why This Milestone Now

P3-M1 established a working single-host Compose baseline and P3-M1.5 validated that path in practice on the current workstation.

That verification also exposed three operator-grade gaps:

- secret and env handling still depends too much on manual discipline
- the current image-build path is not portable enough across pilot hosts
- diagnostics are present but still too manual to gather consistently during failure handling

P3-M2 should address those gaps without changing product scope.

## Current State Summary

What already exists:

- canonical pilot stack under [deploy/pilot/compose.yaml](/Users/hx/it_company/agent_company/deploy/pilot/compose.yaml)
- real env templates under [deploy/pilot/compose.env.example](/Users/hx/it_company/agent_company/deploy/pilot/compose.env.example), [deploy/pilot/backend.env.example](/Users/hx/it_company/agent_company/deploy/pilot/backend.env.example), and [deploy/pilot/agent_runtime.env.example](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.env.example)
- helper scripts for bring-up, DB bootstrap, and preflight:
  - [scripts/pilot_compose.sh](/Users/hx/it_company/agent_company/scripts/pilot_compose.sh)
  - [scripts/pilot_db_bootstrap.sh](/Users/hx/it_company/agent_company/scripts/pilot_db_bootstrap.sh)
  - [scripts/pilot_stack_preflight.sh](/Users/hx/it_company/agent_company/scripts/pilot_stack_preflight.sh)
- lightweight diagnostics runbook under [docs/runbooks/diagnostics-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/diagnostics-guide.md)

What is still weak:

- no operator-facing env audit to catch placeholders, key mismatches, or unsafe file permissions before startup
- build-time base-image selection is not flexible enough across hosts with different registry access realities
- no standard one-command diagnostics bundle for pilot incidents
- runtime/container expectations are implied, but not explicit enough in the pilot deployment docs

## Scope

P3-M2 covers:

- env-file audit and secret/config baseline checks
- Compose and Dockerfile hardening for more repeatable host-to-host builds
- modest container reliability defaults that remain within the current single-host model
- operator-facing diagnostics capture improvements
- documentation alignment across pilot deployment, secrets, and diagnostics runbooks

P3-M2 does not cover:

- product feature changes
- workflow or approval redesign
- autonomy or background-agent work
- Kubernetes, HA, or multi-host orchestration
- vault or external secret-manager adoption

## Planned Changes

### 1. Secret and config baseline

- add one pilot env-audit helper script that checks:
  - required env files exist
  - placeholder values are gone
  - backend/runtime shared key alignment
  - DB password alignment between Compose and backend env
  - obviously unsafe runtime base URL values for Compose-internal use
  - restrictive file permissions on pilot env files
- document this helper as a required pre-start or pre-handoff step
- keep env-first guidance explicit and consistent with the existing credential-rotation runbook

### 2. Runtime and container reliability baseline

- add explicit container defaults that improve operational behavior without changing architecture, such as:
  - `init: true` where appropriate
  - bounded Docker log rotation settings
  - explicit stop-grace expectations where useful
- keep health-check and restart behavior explicit in docs rather than implicit in Compose only

### 3. Deployment repeatability across pilot hosts

- make pilot image base references configurable rather than hard-coded in Dockerfiles
- expose those build-base values in `deploy/pilot/compose.env.example`
- keep the current validated mirror-friendly defaults, but make host override straightforward
- avoid requiring Dockerfile edits for hosts that need different reachable base registries

### 4. Operational diagnostics guidance

- add one helper that collects a lightweight pilot diagnostics bundle:
  - `docker compose ps`
  - recent container logs
  - backend health
  - runtime health
  - optional backend/runtime diagnostics endpoint responses when credentials are available
- document where operators should store and share that bundle
- align the diagnostics runbook with the new helper and the current pilot workflow

## Planned Deliverables

- [docs/plans/p3-m2-reliability-hardening-plan.md](/Users/hx/it_company/agent_company/docs/plans/p3-m2-reliability-hardening-plan.md)
- one new env audit helper under `scripts/`
- one new diagnostics bundle helper under `scripts/`
- updates to:
  - [deploy/pilot/compose.yaml](/Users/hx/it_company/agent_company/deploy/pilot/compose.yaml)
  - [deploy/pilot/compose.env.example](/Users/hx/it_company/agent_company/deploy/pilot/compose.env.example)
  - [deploy/pilot/backend.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/backend.Dockerfile)
  - [deploy/pilot/agent_runtime.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.Dockerfile)
  - [deploy/pilot/frontend.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/frontend.Dockerfile)
  - [deploy/pilot/README.md](/Users/hx/it_company/agent_company/deploy/pilot/README.md)
  - [docs/runbooks/pilot-deployment-baseline.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-baseline.md)
  - [docs/runbooks/pilot-deployment-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-checklist.md)
  - [docs/runbooks/credential-rotation-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/credential-rotation-runbook.md)
  - [docs/runbooks/diagnostics-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/diagnostics-guide.md)

## Acceptance Criteria

P3-M2 is complete when:

- operators have one script that can reject obviously unsafe or inconsistent pilot env setup
- pilot base-image selection can be adapted per host without editing Dockerfiles
- Compose config reflects explicit runtime/container reliability expectations
- operators have one repeatable diagnostics-bundle command for pilot incidents
- pilot docs consistently describe the same hardened bring-up and troubleshooting path

## Validation Plan

- env-audit helper succeeds against the current validated pilot env files
- Compose config still renders cleanly via `bash scripts/pilot_compose.sh config`
- diagnostics bundle helper runs successfully against the live pilot stack
- existing pilot preflight still passes
- one login smoke and one runtime-backed preview smoke still pass after hardening changes

## Assumptions

- the current Compose-first single-host pilot model remains the canonical deployment path
- env files remain the practical pilot secret-injection mechanism for this phase
- lightweight diagnostics collection is sufficient for pilot operation even without centralized observability
