# Pilot Deployment Baseline

This directory contains the Compose-first P3-M1 pilot deployment baseline for AICoOS.

## Canonical Model

The approved pilot topology is:

1. MySQL
2. `agent_runtime`
3. Spring Boot backend
4. frontend or reverse proxy

This baseline is:

- single host
- single environment
- Docker Compose first
- env-first for application secrets
- database-backed config fallback only where the backend already supports it

## Primary Files

- `compose.yaml`
  - canonical pilot stack definition
- `compose.env.example`
  - Compose-only deployment wiring template
- `backend.env.example`
  - backend container environment template
- `agent_runtime.env.example`
  - runtime container environment template
- `backend.Dockerfile`
  - backend container build
- `agent_runtime.Dockerfile`
  - runtime container build
- `frontend.Dockerfile`
  - frontend build and nginx packaging
- `nginx.conf`
  - static serving plus `/api` reverse proxy

## Operator Helpers

- [scripts/pilot_compose.sh](/Users/hx/it_company/agent_company/scripts/pilot_compose.sh)
  - wrapper around the canonical Compose file and env file
  - automatically packages the backend jar before `build` and `up --build`
- [scripts/pilot_env_audit.sh](/Users/hx/it_company/agent_company/scripts/pilot_env_audit.sh)
  - checks pilot env files for placeholders, key mismatches, unsafe runtime URL choices, and weak file permissions
- [scripts/pilot_db_bootstrap.sh](/Users/hx/it_company/agent_company/scripts/pilot_db_bootstrap.sh)
  - imports the base admin schema, repeatable admin seed, template permissions, and repeatable phase1 demo seed into the running pilot MySQL container
- [scripts/pilot_stack_preflight.sh](/Users/hx/it_company/agent_company/scripts/pilot_stack_preflight.sh)
  - host-side reachability and health verification
- [scripts/pilot_collect_diagnostics.sh](/Users/hx/it_company/agent_company/scripts/pilot_collect_diagnostics.sh)
  - collects a lightweight pilot diagnostics bundle with health snapshots, logs, and optional diagnostics endpoint payloads

Fallback references retained for compatibility only:

- `aicoos-backend.service.example`
- `aicoos-agent-runtime.service.example`
- [scripts/start_backend_pilot.sh](/Users/hx/it_company/agent_company/scripts/start_backend_pilot.sh)
- [scripts/start_agent_runtime_pilot.sh](/Users/hx/it_company/agent_company/scripts/start_agent_runtime_pilot.sh)

## Quick Start

1. Copy and edit:
   - `compose.env.example -> compose.env`
   - `backend.env.example -> backend.env`
   - `agent_runtime.env.example -> agent_runtime.env`
2. Restrict the copied env files to owner-only permissions:

```bash
chmod 600 deploy/pilot/compose.env deploy/pilot/backend.env deploy/pilot/agent_runtime.env
```

3. Replace all placeholder secrets before startup.
4. Audit the env files:

```bash
bash scripts/pilot_env_audit.sh
```

5. Render the stack config:

```bash
bash scripts/pilot_compose.sh config
```

6. Start the pilot stack:

```bash
bash scripts/pilot_compose.sh up -d --build
```

7. Bootstrap the base admin schema and seed:

```bash
bash scripts/pilot_db_bootstrap.sh
```

8. Run preflight:

```bash
bash scripts/pilot_stack_preflight.sh
```

9. Open the frontend at `http://127.0.0.1:8080` unless `compose.env` changes the public bind host or port.

## Config Precedence

Use these layers in order:

1. `deploy/pilot/compose.env`
   - Compose project name, image tags, build-base image overrides, host bind ports, volume names, env-file paths, and container log rotation defaults
2. `deploy/pilot/backend.env` and `deploy/pilot/agent_runtime.env`
   - application-level secrets and service configuration injected into containers
3. backend `sys_system_config`
   - non-secret operator-facing defaults and toggles
4. application code defaults
   - bootstrap convenience only

The backend still resolves system-config-backed keys as `environment -> database -> default`.

Current reliability defaults in the Compose baseline:

- `init: true` on non-MySQL containers
- bounded Docker JSON log rotation via `PILOT_LOG_MAX_SIZE` and `PILOT_LOG_MAX_FILE`
- configurable stop-grace periods per service
- configurable build-base image references so pilot hosts can swap registry mirrors without editing Dockerfiles

## Startup Order

Compose health dependencies preserve the expected order:

1. database
2. `agent_runtime`
3. backend
4. frontend

Use the Compose baseline here as the primary pilot handoff path, not the old supervisor-first model.
