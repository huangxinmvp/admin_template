# P3-M1 Pilot Deployment Baseline Plan

## Goal

Define one controlled, repeatable pilot deployment baseline for AICoOS that can be handed off without platform engineering work.

P3-M1 should standardize on:

- one single environment
- one single host
- one canonical deployment path
- one explicit startup sequence
- one explicit config precedence model
- one practical upgrade and rollback story

The preferred baseline is single-host Docker Compose. Existing supervised-process artifacts remain useful as fallback references, but they should no longer be treated as the primary pilot operating model.

## Why This Milestone Now

The repo already contains most of the pilot-readiness ingredients:

- local MySQL bootstrap via [docker-compose.local.yml](/Users/hx/it_company/agent_company/docker-compose.local.yml)
- pilot env examples under [deploy/pilot](/Users/hx/it_company/agent_company/deploy/pilot)
- backend and runtime pilot start scripts
- preflight checks
- deployment, upgrade, and rollback runbooks

What is still missing is a single operator-facing deployment baseline that is easier to repeat than host-level manual process supervision. Today the repo reads as:

- Compose for local MySQL only
- `systemd` examples for pilot
- manual sequencing for the rest of the stack

For P3-M1, that should become:

- one Compose-defined pilot stack
- one release packaging shape
- one documented handoff flow

## Scope

P3-M1 covers:

- a Compose-first single-host deployment baseline
- container packaging for backend, runtime, and frontend
- explicit startup/readiness sequencing
- explicit pilot config precedence and ownership rules
- pilot handoff documentation
- practical upgrade and rollback guidance for the chosen baseline

P3-M1 does not cover:

- Kubernetes
- HA or failover
- multi-environment platformization
- zero-downtime deploys
- vault or external secret-manager adoption
- autonomous runtime expansion

## Target Deployment Model

The canonical pilot shape should be:

1. one MySQL container with a named persistent volume
2. one `agent_runtime` container
3. one Spring Boot backend container
4. one frontend container or simple reverse-proxy/static-serving container

Operational rules:

- all services run on one host under one Docker Compose project
- only the frontend or reverse proxy is intended for browser access
- `agent_runtime` stays on the internal Compose network
- backend remains the only business system entrypoint
- image tags are explicit and versioned per pilot release
- the database may remain on the same host for this milestone because repeatability is more important than infra sophistication

If a pilot host cannot use Docker Compose, the existing `systemd` examples may be kept as an equivalent simple fallback, but the plan and runbooks should optimize for Compose first.

## Planned Changes

### 1. Canonical pilot deployment artifacts

- add a canonical pilot Compose file under `deploy/pilot/`, such as `compose.yaml`
- add Dockerfiles for:
  - Spring Boot backend jar packaging
  - `agent_runtime`
  - frontend static serving or reverse-proxy packaging
- add one Compose-level env example for deployment wiring concerns:
  - image tags
  - host ports
  - project name
  - mounted data/log paths if needed
- retain service-specific env examples for backend and runtime
- treat `deploy/pilot/*.service.example` as compatibility references, not the primary handoff path

### 2. Startup order and readiness model

- encode startup dependencies in Compose with health checks where possible
- keep the startup order explicit and unchanged:
  1. database
  2. `agent_runtime`
  3. backend
  4. frontend or reverse proxy
- keep backend bootstrap as the owner of:
  - Flowable schema bootstrap
  - AICoOS bootstrap SQL
  - system-config seed alignment
- update pilot preflight so it validates the Compose baseline rather than only host-level processes
- make shutdown/restart guidance explicit for maintenance and upgrades

### 3. Config precedence and ownership

Define one pilot config policy and repeat it consistently across docs and examples:

1. host-injected env values and Compose env files
2. service container environment variables
3. backend database `sys_system_config` for approved non-secret operator settings
4. application defaults for bootstrap convenience only

Ownership rules:

- secrets belong in env files or equivalent host-side injection, not in the database unless retained temporarily for compatibility
- Compose-level env files should hold deployment wiring and image/version values
- backend env files should hold DB connectivity, JWT secret, runtime base URL, and integration secrets
- runtime env files should hold provider/model/base-url/key settings
- system-config should remain limited to non-secret business toggles and safe operator-visible defaults

This preserves the current backend precedence of `environment -> database -> default`, but makes the pilot operating source of truth much clearer.

### 4. Release packaging and handoff

- define one release bundle for pilot operators:
  - Compose file
  - version-pinned image references
  - env example files
  - preflight command
  - pilot checklist link
- document the minimal host prerequisites:
  - Docker Engine
  - Docker Compose plugin
  - writable persistent storage for MySQL volume
  - operator access to env files and logs
- make `deploy/pilot/README.md` the short entrypoint for operators
- align [docs/runbooks/pilot-deployment-baseline.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-baseline.md) with the Compose-first baseline

### 5. Upgrade and rollback guidance

Upgrade guidance should assume:

- image tags are the deployable unit
- database backup happens before any upgrade
- environment file changes are tracked alongside the release
- post-upgrade success is based on health checks plus a small browser smoke pass

Rollback guidance should assume:

- the previous image tags remain known and recoverable
- rollback normally means re-pinning previous images and restarting the Compose stack
- database restore is only required when bootstrap/schema/data changes make the environment unsafe
- no zero-downtime or blue/green behavior is required for this milestone

## Documentation Deliverables

P3-M1 should update the existing pilot docs so they all tell the same story:

- [deploy/pilot/README.md](/Users/hx/it_company/agent_company/deploy/pilot/README.md)
- [docs/runbooks/pilot-deployment-baseline.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-baseline.md)
- [docs/runbooks/pilot-deployment-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-checklist.md)
- [docs/runbooks/deployment-preparation.md](/Users/hx/it_company/agent_company/docs/runbooks/deployment-preparation.md)
- [docs/runbooks/environment-config-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/environment-config-guide.md)
- [docs/runbooks/upgrade-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/upgrade-runbook.md)
- [docs/runbooks/rollback-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/rollback-runbook.md)
- [docs/runbooks/pilot-environment-boundaries.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-environment-boundaries.md)

## Acceptance Criteria

P3-M1 is complete when:

- one operator can deploy the full pilot stack on one host with Docker Compose
- startup order is encoded and documented clearly enough to avoid ad hoc sequencing
- config precedence is documented without ambiguity
- secrets and non-secret settings have clear ownership rules
- pilot handoff docs point to one canonical deployment path
- upgrade guidance is version/tag based and operationally realistic
- rollback guidance is explicit about when image rollback is enough and when DB restore is required

## Validation Plan

- backend package build succeeds: `./mvnw -q -DskipTests package`
- frontend build succeeds: `npm run build`
- runtime tests still pass: `python3 -m unittest agent_runtime.tests.test_app`
- pilot Compose config renders cleanly: `docker compose -f deploy/pilot/compose.yaml config`
- pilot stack health checks pass after bring-up
- preflight passes against the Compose-hosted stack
- one login flow and one runtime-backed preview flow pass in the pilot baseline

## Assumptions

- the pilot host can run Docker and Docker Compose
- keeping MySQL on the same single host is acceptable for this phase
- backend bootstrap remains the migration/bootstrap mechanism for the current schema needs
- Compose is the primary baseline; supervised non-Compose process management is only a fallback compatibility path
