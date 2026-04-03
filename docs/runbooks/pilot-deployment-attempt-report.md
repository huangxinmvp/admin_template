# Pilot Deployment Attempt Report

## Goal

Perform one real controlled pilot deployment attempt using the current Compose-first baseline, helper scripts, and runbooks.

This is an execution and verification report, not a product milestone note.

## Attempt Date

- `2026-04-03`

## Actual Host Used

This report now covers two concrete execution results from `2026-04-03`:

1. one successful controlled-host deployment attempt on the currently accessible local Docker host
2. one blocked remote target-host access attempt against `101.132.145.6`

The successful deployment execution was run on the currently accessible controlled deployment host:

- Docker context: `desktop-linux`
- Docker engine: `docker-desktop | 28.2.2 | Docker Desktop`
- Host OS: `macOS 26.3.1 (arm64)`

This means the successful deployment sequence below is a **real host deployment attempt on the current controlled host**, but it is **not yet a separately proven remote-host handoff**.

## Remote Target-Host Access Attempt

Provided target host:

- `101.132.145.6`
- requested account: `root`

What was verified from this environment:

- TCP reachability to SSH on port `22`
- remote SSH banner:
  - `OpenSSH_8.0`
- advertised authentication methods:
  - `publickey`
  - `gssapi-keyex`
  - `gssapi-with-mic`
  - `password`

What failed:

- repeated password-based SSH login as `root` was rejected by the server
- remote access therefore stopped before any env-file preparation, file transfer, Docker inspection, or Compose execution could begin

Interpretation:

- the host is reachable
- the SSH service is alive
- password authentication is offered by the server
- but the currently provided `root` credential did not authenticate successfully from this environment

## Scope Executed

This attempt covered:

1. confirming real pilot env files were present on the host
2. auditing those env files
3. confirming configured base-image sources were reachable from the host
4. running the documented Compose deployment sequence
5. verifying container health and service reachability
6. running authenticated backend and runtime smoke checks
7. running a minimal browser smoke check on the host
8. collecting a diagnostics bundle

No product features were added.

## Env File Status

The following real env files were already present on the host and were used for the attempt:

- [compose.env](/Users/hx/it_company/agent_company/deploy/pilot/compose.env)
- [backend.env](/Users/hx/it_company/agent_company/deploy/pilot/backend.env)
- [agent_runtime.env](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.env)

Audit result:

- `bash scripts/pilot_env_audit.sh`
- result: pass
- warnings: `0`

## Image Source / Registry Check

The current host did not override the pilot base-image variables in `compose.env`, so the default Compose image sources were used:

- `docker.m.daocloud.io/library/maven:3.9.9-eclipse-temurin-21`
- `docker.m.daocloud.io/library/python:3.12-slim`
- `docker.m.daocloud.io/library/node:20-bookworm-slim`
- `docker.m.daocloud.io/library/nginx:1.27-alpine`

Host-side pull verification was executed with `docker pull` for each image above.

Result:

- all four pulls succeeded
- each image reported `Image is up to date`

## Deployment Sequence Executed

### 1. Env Audit

Command:

```bash
bash scripts/pilot_env_audit.sh
```

Result:

- pass

### 2. Compose Up

Command:

```bash
bash scripts/pilot_compose.sh up -d --build
```

First result:

- failed

Observed blocker:

- [frontend.Dockerfile](/Users/hx/it_company/agent_company/deploy/pilot/frontend.Dockerfile) declared `FRONTEND_NGINX_BASE_IMAGE` too late for the second `FROM`
- BuildKit resolved the second stage with a blank base image and stopped the deployment

Small unblocker fix applied:

- move `ARG FRONTEND_NGINX_BASE_IMAGE=...` into global Dockerfile scope before the first `FROM`

Second result after that fix:

- pass
- Compose rebuilt and recreated:
  - `mysql`
  - `agent_runtime`
  - `backend`
  - `frontend`

### 3. Database Bootstrap

Command:

```bash
bash scripts/pilot_db_bootstrap.sh
```

Result:

- pass
- base schema already present, so `create.sql` was skipped
- repeatable admin seed imported
- repeatable permission bootstrap imported
- repeatable phase1 demo seed imported

### 4. Stack Preflight

Command:

```bash
bash scripts/pilot_stack_preflight.sh
```

Result:

- pass
- MySQL reachable on `127.0.0.1:3306`
- runtime health reachable on `127.0.0.1:8091/health`
- backend health reachable on `127.0.0.1:8081/api/system/health`
- frontend reachable on `127.0.0.1:8080/`

### 5. Diagnostics Bundle

Command:

```bash
bash scripts/pilot_collect_diagnostics.sh /tmp/aicoos-pilot-diagnostics
```

Result:

- pass
- bundle created at:
  - `/tmp/aicoos-pilot-diagnostics/20260403-080324`

Verified bundle contents:

- `notes/compose-ps.txt`
- `logs/mysql.log`
- `logs/agent_runtime.log`
- `logs/backend.log`
- `logs/frontend.log`
- `api/backend-health.json`
- `api/runtime-health.json`
- `api/frontend-index.html`
- `api/auth-login.json`
- `api/backend-diagnostics.json`
- `api/runtime-diagnostics.json`

Token handling verification:

- stored `auth-login.json` redacted access and refresh tokens as expected

## Service State After Deployment

Final `docker compose ps -a` state:

- `mysql`: `healthy`
- `agent_runtime`: `healthy`
- `backend`: `healthy`
- `frontend`: `healthy`

All four pilot services remained up after the deployment sequence completed.

## Authenticated Smoke Checks

The following host-side authenticated checks succeeded after bring-up:

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/system/dashboard`
- `POST /api/aicoos/decisionItem/demo_decision_alpha_01/budget-impact-suggestion`

Observed results:

- login returned a valid access token
- `/api/auth/me` succeeded
- `/api/system/dashboard` succeeded
- runtime-backed preview succeeded

## Browser Smoke

Minimal browser verification was executed on the host using Safari.

Observed result:

- frontend opened successfully
- active URL resolved to:
  - `http://127.0.0.1:8080/user/login?redirect=%2F`
- observed page title:
  - `登录 - SaaS Admin Template`

Interpretation:

- browser reachability through the frontend container is confirmed
- authenticated browser navigation was not separately automated in this round
- post-login functional confidence for this attempt comes from the authenticated API smoke checks plus healthy frontend/backend/runtime state

## What Worked

- real host env files were usable without further edits
- env audit passed with zero warnings
- current mirrored base images were reachable from the host
- the full documented deployment sequence completed after one small Dockerfile unblocker fix
- all containers ended healthy
- host-side preflight passed
- backend login and protected API smoke passed
- one runtime-backed preview flow passed
- diagnostics bundle generation passed
- minimal browser reachability passed

## What Failed

- the first `pilot_compose.sh up -d --build` attempt failed because the frontend Dockerfile declared the nginx base-image `ARG` too late for the second `FROM`
- the remote target-host attempt against `101.132.145.6` did not pass SSH authentication for `root`

The frontend Dockerfile blocker was a real practical deployment issue and was fixed during the attempt.

The remote-host SSH blocker is still unresolved.

## Practical Blockers Still Remaining

- the remote target host `101.132.145.6` is reachable, but `root` SSH authentication is currently failing from this environment
- the validated image path still depends on the current host being able to reach `docker.m.daocloud.io`, or on equivalent preloaded images
- first-time DB bootstrap still requires an explicit `bash scripts/pilot_db_bootstrap.sh` step after `compose up`
- browser smoke in this round confirmed reachability only; a fully automated browser login/dashboard assertion was not added

## Verdict

Controlled pilot deployment is **practically achievable on the current controlled host** using the documented Compose-first path.

What is now practically validated:

- env preparation and env auditing
- registry reachability for the currently configured mirrored base images
- `pilot_compose.sh up -d --build`
- `pilot_db_bootstrap.sh`
- `pilot_stack_preflight.sh`
- `pilot_collect_diagnostics.sh`
- backend login and protected API verification
- one minimal browser reachability check

What is **not yet fully proven** by this report:

- a real handoff onto a separately administered remote pilot host
- host portability where the current mirrored base-image path is unavailable

## Remaining Blockers Before Real Pilot Handoff

1. Run the same sequence on the actual remote pilot host, not only on the current controlled Docker Desktop host.
2. Fix SSH access for the target host `101.132.145.6`, either by correcting the `root` credential or by providing a permitted SSH user / key path.
3. Confirm the remote host can reach the current mirrored image sources, or preload the required base images there.
4. Prepare the remote host's real env files and rerun `pilot_env_audit.sh`.
5. Decide whether the explicit `pilot_db_bootstrap.sh` step is acceptable in the handoff checklist, or whether operators require a more automated first-run path.
6. Capture one stronger browser-side handoff artifact on the real pilot host if the handoff requires visual proof beyond URL/title reachability.

## Evidence

Primary evidence for this deployment attempt:

- `/tmp/aicoos-pilot-diagnostics/20260403-080324`
