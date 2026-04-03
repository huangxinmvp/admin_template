# Pilot Deployment Baseline

## Goal

Provide one concrete, repeatable pilot deployment baseline for the current AICoOS stack.

This runbook is for controlled pilot use, not for production platformization.

## Baseline Model

The current recommended pilot shape is:

1. one MySQL container
2. one `agent_runtime` container
3. one Spring Boot backend container
4. one frontend nginx container that serves static assets and proxies `/api` to the backend

Operational baseline:

- one host
- one Docker Compose project
- one public browser entrypoint
- backend and runtime bound to host loopback for operator diagnostics
- no HA
- no Kubernetes
- no multi-environment orchestration

## Artifacts

Use these files as the canonical pilot package:

- [deploy/pilot/compose.yaml](/Users/hx/it_company/agent_company/deploy/pilot/compose.yaml)
- [deploy/pilot/compose.env.example](/Users/hx/it_company/agent_company/deploy/pilot/compose.env.example)
- [deploy/pilot/backend.env.example](/Users/hx/it_company/agent_company/deploy/pilot/backend.env.example)
- [deploy/pilot/agent_runtime.env.example](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.env.example)
- [deploy/pilot/backend.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/backend.Dockerfile)
- [deploy/pilot/agent_runtime.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.Dockerfile)
- [deploy/pilot/frontend.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/frontend.Dockerfile)
- [deploy/pilot/nginx.conf](/Users/hx/it_company/agent_company/deploy/pilot/nginx.conf)
- [scripts/pilot_compose.sh](/Users/hx/it_company/agent_company/scripts/pilot_compose.sh)
- [scripts/pilot_env_audit.sh](/Users/hx/it_company/agent_company/scripts/pilot_env_audit.sh)
- [scripts/pilot_db_bootstrap.sh](/Users/hx/it_company/agent_company/scripts/pilot_db_bootstrap.sh)
  - imports the base admin schema, repeatable admin seed, template permissions, and repeatable phase1 demo seed
- [scripts/pilot_stack_preflight.sh](/Users/hx/it_company/agent_company/scripts/pilot_stack_preflight.sh)
- [scripts/pilot_collect_diagnostics.sh](/Users/hx/it_company/agent_company/scripts/pilot_collect_diagnostics.sh)

## 1. Prepare Env Files

Copy:

- `deploy/pilot/compose.env.example -> deploy/pilot/compose.env`
- `deploy/pilot/backend.env.example -> deploy/pilot/backend.env`
- `deploy/pilot/agent_runtime.env.example -> deploy/pilot/agent_runtime.env`

Replace all placeholder secrets before startup.

Recommended file-permission baseline after copying:

```bash
chmod 600 deploy/pilot/compose.env deploy/pilot/backend.env deploy/pilot/agent_runtime.env
```

Required pilot-sensitive values to replace:

- `MYSQL_APP_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `DB_PASSWORD`
- `APP_JWT_SECRET`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`
- `AGENT_RUNTIME_API_KEY`
- provider or integration API keys if enabled

## 2. Understand Config Ownership

Use these layers in order:

1. `compose.env`
   - host bind ports
   - image tags
   - build-base image overrides
   - Compose project and volume names
   - service env file paths
   - container log rotation and stop-grace defaults
2. `backend.env` and `agent_runtime.env`
   - container-injected application values
3. backend `sys_system_config`
   - non-secret UI-facing defaults and toggles
4. code defaults
   - bootstrap convenience only

Important:

- the backend still applies `environment -> database -> default`
- the runtime reads environment variables directly
- the frontend is built to use same-origin `/api` by default, so nginx is the pilot routing layer

## 3. Render And Start The Stack

Render the final Compose config first:

```bash
bash scripts/pilot_compose.sh config
```

Current helper behavior:

- `pilot_compose.sh` runs `bash scripts/pilot_env_audit.sh` before `config`, `build`, and `up`
- `pilot_compose.sh` packages the backend jar on the host before `build` and `up --build`

Start the stack:

```bash
bash scripts/pilot_compose.sh up -d --build
```

Startup order is encoded as:

1. database
2. `agent_runtime`
3. backend
4. frontend

On first bring-up, bootstrap the base admin schema and repeatable admin seed:

```bash
bash scripts/pilot_db_bootstrap.sh
```

## 4. Run Preflight And Health Checks

Run:

```bash
bash scripts/pilot_stack_preflight.sh
```

Default health targets:

- frontend: [http://127.0.0.1:8080/](http://127.0.0.1:8080/)
- backend: [http://127.0.0.1:8081/api/system/health](http://127.0.0.1:8081/api/system/health)
- runtime: [http://127.0.0.1:8091/health](http://127.0.0.1:8091/health)

Expected backend payload shape:

- `status`
- `application`
- `databaseReady`
- `timestamp`

Expected runtime payload shape:

- `status`
- `runtimeKeyConfigured`
- `defaultProviderReady`
- `mockFallbackEnabled`

## 5. Minimal Pilot Verification

After preflight passes, confirm:

1. login works through the frontend container
2. config center still masks password-style values
3. one runtime-backed preview flow works
4. Linear or Figma preview/apply is verified only if enabled in the pilot env

Current internal runtime hostname for the Compose baseline:

- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL=http://agent-runtime:8091`

Current runtime/container reliability defaults in the Compose baseline:

- `init: true` on backend, runtime, and frontend
- bounded container log rotation
- explicit stop-grace periods per service
- configurable build-base image references per host

## Fallback Path

The `systemd` examples and host-level pilot start scripts remain in the repo as fallback references.

They are not the primary pilot deployment story anymore.

## Diagnostics Collection

For pilot incident capture, run:

```bash
bash scripts/pilot_collect_diagnostics.sh /tmp/aicoos-pilot-diagnostics
```

The bundle includes:

- `docker compose ps`
- recent container logs
- backend and runtime health payloads
- frontend entry HTML
- optional backend login and diagnostics payload
- optional runtime diagnostics payload

## Current Limits

This baseline does not provide:

- HA
- zero-downtime deploys
- vault-backed secret storage
- automated multi-environment rollout
- autonomous agent execution
