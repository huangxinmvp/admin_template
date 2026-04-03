# Environment and Config Guide

## Goal

Consolidate the current environment and configuration rules for operating AICoOS in:

- local development
- controlled demo usage
- controlled pilot usage

This guide is not a production operations manual.

## Service Topology

Current core services:

1. MySQL
2. Spring Boot backend
3. `agent_runtime`
4. frontend

Boundary rules:

- Spring Boot remains the business source of truth
- `agent_runtime` remains a separate suggestion service
- frontend should talk to Spring Boot, not directly to `agent_runtime`
- external tool calls should remain behind Spring Boot

## Environment Classes

### Local

Typical characteristics:

- frontend dev server with local proxy
- local MySQL
- mock runtime by default
- real integrations optional

Typical startup:

```bash
docker compose -f docker-compose.local.yml up -d mysql
bash scripts/start_agent_runtime_mock.sh
bash scripts/start_backend_local.sh
cd web_frontend && npm run start:dev
bash scripts/local_stack_preflight.sh
```

### Demo

Typical characteristics:

- stable local or shared environment
- real integrations may be enabled
- still not equivalent to production

### Pilot

Typical characteristics:

- single host
- single environment
- Compose-first deployment
- real integrations expected only when pilot scope needs them
- frontend served by nginx, not a dev server
- backend and runtime exposed to host loopback for diagnostics only

Pilot references:

- [deploy/pilot/README.md](/Users/hx/it_company/agent_company/deploy/pilot/README.md)
- [deploy/pilot/compose.yaml](/Users/hx/it_company/agent_company/deploy/pilot/compose.yaml)
- [scripts/pilot_compose.sh](/Users/hx/it_company/agent_company/scripts/pilot_compose.sh)
- [scripts/pilot_env_audit.sh](/Users/hx/it_company/agent_company/scripts/pilot_env_audit.sh)
- [scripts/pilot_stack_preflight.sh](/Users/hx/it_company/agent_company/scripts/pilot_stack_preflight.sh)

## Config Layers

### Layer 1: Compose deployment wiring

Use [compose.env.example](/Users/hx/it_company/agent_company/deploy/pilot/compose.env.example) for:

- image tags
- build-base image overrides
- host bind ports
- Compose project and volume names
- env-file locations
- container log rotation defaults

These values are consumed by Compose itself and do not replace backend database config rules.

### Layer 2: Container-injected application config

Use:

- [backend.env.example](/Users/hx/it_company/agent_company/deploy/pilot/backend.env.example)
- [agent_runtime.env.example](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.env.example)

for application-level values injected into containers.

Pilot secret-handling baseline:

- copied pilot env files should be restricted to owner-only access such as `chmod 600`
- run `bash scripts/pilot_env_audit.sh` before `config`, `build`, or `up`
- keep Compose-internal URLs on service DNS names, not `127.0.0.1`

### Layer 3: Backend system-config

Use backend `sys_system_config` for:

- branding
- non-secret feature toggles
- operator-visible defaults
- safe tuning values

### Layer 4: Code defaults

Treat code defaults as bootstrap convenience only.

## Backend Precedence

For backend system-config-backed keys, the effective order remains:

1. environment override
2. database system-config
3. code default

This is implemented in:

- [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)

Operational rule:

- use env files for secrets and environment-specific endpoints
- use database system-config for non-secret toggles and operator-facing defaults
- do not rely on code defaults as pilot policy

## Backend Environment Notes

Important backend inputs:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_JWT_SECRET`
- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`

In the Compose pilot baseline:

- `DB_URL` should point to `mysql`
- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL` should point to `http://agent-runtime:8091`
- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL` should not use `127.0.0.1`, `localhost`, or underscore hostnames

## Runtime Environment Notes

Important runtime inputs:

- `AGENT_RUNTIME_API_KEY`
- `AGENT_RUNTIME_PROVIDER`
- `AGENT_RUNTIME_MODEL`
- `AGENT_RUNTIME_TIMEOUT_SECONDS`
- `AGENT_RUNTIME_ALLOW_MOCK_FALLBACK`
- `OPENAI_COMPATIBLE_BASE_URL`
- `OPENAI_COMPATIBLE_API_KEY`

Recommended usage:

- local: mock provider is the safest default
- demo: mock or real provider depending on the session goal
- pilot: use explicit provider configuration and keep provider secrets in runtime env only

## Frontend Environment Notes

The built frontend supports `REACT_APP_API_BASE_URL`, but the pilot default is same-origin `/api` through nginx.

Important boundary:

- Umi proxy is a development convenience
- it does not solve pilot deployment routing
- the Compose pilot baseline uses nginx reverse proxying, not the dev proxy

## System-Config Ownership Rules

### Put in environment files

Use env files first for:

- DB connectivity
- runtime provider secrets
- Spring Boot to runtime shared keys
- Linear API key
- Figma API key
- environment-specific service base URLs

### Put in database system-config

Use system-config for:

- branding
- non-secret feature and integration toggles
- operator-visible defaults
- safe tuning values that do not create secret exposure risk
