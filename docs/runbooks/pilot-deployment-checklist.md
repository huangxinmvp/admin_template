# Pilot Deployment Checklist

## Goal

Provide one clear checklist for standing up the current AICoOS pilot environment with the Compose-first baseline.

This checklist is for controlled pilot environments, not full production rollout.

## Required Components

Deploy and verify all of the following:

- MySQL-compatible database container
- `agent_runtime` container
- Spring Boot backend container
- frontend nginx container
- Flowable schema and process tables through backend bootstrap

## Required Configuration Files

Before startup, prepare all of these:

- `deploy/pilot/compose.env`
- `deploy/pilot/backend.env`
- `deploy/pilot/agent_runtime.env`

Copy them from the matching `.example` files and replace placeholder secrets.

After copying, restrict the env files to owner-only access such as:

```bash
chmod 600 deploy/pilot/compose.env deploy/pilot/backend.env deploy/pilot/agent_runtime.env
```

## Required Configuration Inputs

### Compose wiring

- `MYSQL_APP_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- image tags if not using defaults
- build-base image overrides if the pilot host needs a different reachable registry path
- host bind ports if the defaults do not fit the pilot host

### Backend

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_JWT_SECRET`
- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`

### Runtime

- `AGENT_RUNTIME_API_KEY`
- `AGENT_RUNTIME_PROVIDER`
- `AGENT_RUNTIME_MODEL`

Provider-dependent:

- `OPENAI_COMPATIBLE_BASE_URL`
- `OPENAI_COMPATIBLE_API_KEY`
- or compatible fallback names if used by the environment

### Integrations

Linear:

- `AICOOS_CONFIG_INTEGRATION_LINEAR_ENABLED`
- `AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY`
- `AICOOS_CONFIG_INTEGRATION_LINEAR_DEFAULT_TEAM_ID`

Figma:

- `AICOOS_CONFIG_INTEGRATION_FIGMA_ENABLED`
- `AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY`

## Startup Order

Use this order:

1. database
2. `agent_runtime`
3. backend
4. frontend
5. preflight and browser verification

Canonical commands:

```bash
bash scripts/pilot_env_audit.sh
bash scripts/pilot_compose.sh config
bash scripts/pilot_compose.sh up -d --build
bash scripts/pilot_db_bootstrap.sh
bash scripts/pilot_stack_preflight.sh
```

## Startup And Health Checks

### Database

- [ ] DB container is healthy before backend start
- [ ] target schema exists or can be created

### Runtime

- [ ] `GET /health` returns healthy
- [ ] `defaultProviderReady=true`
- [ ] `runtimeKeyConfigured=true`

### Backend

- [ ] backend starts on expected host loopback port
- [ ] Flowable bootstrap completes cleanly
- [ ] bootstrap SQL completes cleanly
- [ ] `bash scripts/pilot_db_bootstrap.sh` completes cleanly on first bring-up
- [ ] template permissions and phase1 demo seed are present after bootstrap when the pilot baseline expects demo data
- [ ] `GET /api/system/health` returns healthy with `databaseReady=true`
- [ ] `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL` uses the Compose-internal runtime hostname, not `127.0.0.1` or `agent_runtime`

### Frontend

- [ ] frontend is reachable on the configured public port
- [ ] `/api` requests route through nginx to the backend correctly

## Integration Verification

### Linear

- [ ] Linear is enabled only if pilot scope requires it
- [ ] valid API key is injected
- [ ] valid default team id is present if create mode relies on it
- [ ] one `preview -> apply` mapping flow succeeds

### Figma

- [ ] Figma is enabled only if pilot scope requires it
- [ ] valid API key is injected
- [ ] one confirmed accessible file or node is available
- [ ] one `preview -> apply` bind flow succeeds

## Operational Verification

Before pilot signoff, confirm:

- [ ] login works through the frontend container
- [ ] dashboard loads
- [ ] project center loads
- [ ] runtime-backed suggestion preview/apply flows work
- [ ] approval actions work
- [ ] diagnostics endpoints are reachable when needed
- [ ] `bash scripts/pilot_collect_diagnostics.sh /tmp/aicoos-pilot-diagnostics` produces a usable diagnostics bundle

## Pilot Handoff Ready

Mark the environment ready for controlled pilot handoff only if:

- [ ] all required containers are deployed
- [ ] all required env files are present
- [ ] startup order is reproducible through Compose
- [ ] health checks pass
- [ ] core browser walkthrough passes
- [ ] upgrade and rollback expectations are understood

## Boundary Reminder

This checklist supports controlled pilot use only.

It does not imply:

- production readiness
- HA readiness
- full observability coverage
- autonomous operation readiness
