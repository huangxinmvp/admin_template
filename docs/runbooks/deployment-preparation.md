# Deployment Preparation Notes

## Goal

Capture the current deployment assumptions and minimum hardening expectations for moving AICoOS into a controlled pilot baseline.

This is not a full production operations guide.

## Recommended Baseline

Current P3-M1 reference shape:

1. one MySQL-compatible database container
2. one `agent_runtime` container
3. one Spring Boot backend container
4. one frontend nginx container

Reference artifacts:

- [pilot-deployment-baseline.md](/Users/hx/it_company/agent_company/docs/runbooks/pilot-deployment-baseline.md)
- [deploy/pilot/README.md](/Users/hx/it_company/agent_company/deploy/pilot/README.md)
- [deploy/pilot/compose.yaml](/Users/hx/it_company/agent_company/deploy/pilot/compose.yaml)

## Runtime Separation

- keep Spring Boot as the business source of truth
- keep `agent_runtime` as a separate suggestion service
- do not expose `agent_runtime` directly to browsers
- keep external integration calls behind Spring Boot
- let nginx and the frontend container be the browser-facing pilot entrypoint

## Config Hygiene

### Pilot deployment wiring

Use [compose.env.example](/Users/hx/it_company/agent_company/deploy/pilot/compose.env.example) for:

- image tags
- host bind ports
- Compose project and volume names
- service env file locations

### Pilot application secrets and service config

Use:

- [backend.env.example](/Users/hx/it_company/agent_company/deploy/pilot/backend.env.example)
- [agent_runtime.env.example](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.env.example)

for:

- JWT secret
- runtime shared key
- runtime provider credentials
- integration API keys
- backend DB connection values injected into the backend container

### Backend precedence

For system-config-backed backend keys, the effective order remains:

1. environment variable
2. database system-config
3. code default

Use database config for:

- non-secret business toggles
- UI-facing defaults
- safe operator-visible configuration

Keep secrets out of database config when possible.

## Health And Startup Consistency

Preferred startup order:

1. database
2. `agent_runtime`
3. backend
4. frontend

Health endpoints:

- frontend: `GET /`
- backend: `GET /api/system/health`
- runtime: `GET /health`

Minimum expectations:

- database is healthy before backend starts
- runtime health returns `defaultProviderReady=true`
- backend health returns `databaseReady=true`
- frontend serves the built app and proxies `/api` successfully

## Operational Artifacts

P3-M1 now uses these pilot operator artifacts:

- `deploy/pilot/compose.yaml`
- `deploy/pilot/compose.env.example`
- `deploy/pilot/backend.env.example`
- `deploy/pilot/agent_runtime.env.example`
- `scripts/pilot_compose.sh`
- `scripts/pilot_stack_preflight.sh`

Fallback artifacts retained only for compatibility:

- `deploy/pilot/*.service.example`
- `scripts/start_backend_pilot.sh`
- `scripts/start_agent_runtime_pilot.sh`

## Observability Expectations

Current platform state remains lightweight:

- request correlation is available via `X-Request-Id`
- backend and runtime health endpoints are available
- nginx and application container logs are accessible through Docker Compose

For early pilot deployment, at minimum:

- retain `docker compose logs` output during verification
- keep request IDs in backend and runtime logs
- monitor DB availability, backend health, runtime health, and frontend reachability

## Known Production Gaps

- no dedicated secret manager
- no HA deployment model
- no zero-downtime rollout
- no metrics and alerting stack
- no background reconciliation for external integrations
