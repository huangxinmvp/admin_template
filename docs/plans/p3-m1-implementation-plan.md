# P3-M1 Implementation Plan

## Goal

Implement the Phase 1 pilot deployment baseline for AICoOS as a Compose-first, single-environment, single-host deployment package.

The target operating model is:

1. MySQL
2. `agent_runtime`
3. Spring Boot backend
4. frontend or reverse proxy

## Implementation Steps

### 1. Add the Compose deployment skeleton

- add `deploy/pilot/compose.yaml` as the canonical pilot stack
- add container packaging for:
  - backend jar
  - `agent_runtime`
  - frontend static serving plus reverse proxy to backend
- keep the stack single-host and single-environment only

### 2. Add environment template files

- keep `deploy/pilot/backend.env.example`
- keep `deploy/pilot/agent_runtime.env.example`
- add a Compose-level env template for image tags, ports, database defaults, and mount paths
- make config precedence explicit across these templates:
  - Compose env and host env
  - service container env
  - backend system-config for approved non-secret settings
  - code defaults only for bootstrap convenience

### 3. Add startup and preflight support

- keep `scripts/pilot_stack_preflight.sh` as the canonical verification helper
- update it to match the Compose-first baseline and externally exposed pilot URLs
- add minimal helper commands only where they improve repeatability for pilot operators
- rely on existing backend and runtime health endpoints rather than introducing new product logic

### 4. Align runbooks

- update `deploy/pilot/README.md` to be the short operator entrypoint
- update deployment runbooks to describe the Compose-first baseline
- align startup order, config precedence, upgrade flow, and rollback flow with the implemented artifacts

## Intended File Changes

- `deploy/pilot/compose.yaml`
- `deploy/pilot/compose.env.example`
- `deploy/pilot/backend.env.example`
- `deploy/pilot/agent_runtime.env.example`
- container packaging files under `deploy/pilot/`
- `scripts/pilot_stack_preflight.sh`
- `deploy/pilot/README.md`
- pilot deployment runbooks under `docs/runbooks/`

## Validation Plan

- render the Compose config successfully
- verify shell scripts with `bash -n`
- run runtime unit tests if unchanged assumptions still hold
- run backend and frontend build checks when needed for the added packaging artifacts

## Out Of Scope

- Kubernetes
- HA or failover
- multi-environment orchestration
- new business features
- production-grade secret management
