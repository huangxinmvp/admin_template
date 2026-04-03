# Diagnostics Guide

## Goal

Help operators and developers quickly answer four questions during a pilot:

1. what failed
2. where it failed
3. which request id to use
4. what to check next

This guide is intentionally lightweight and matches the current repo state.

Primary helper:

- [scripts/pilot_collect_diagnostics.sh](/Users/hx/it_company/agent_company/scripts/pilot_collect_diagnostics.sh)

## First Checks

Before deeper diagnosis:

1. confirm backend health via:
   - `curl -s http://127.0.0.1:8081/api/system/health`
2. confirm runtime health via:
   - `curl -s http://127.0.0.1:8091/health`
3. confirm frontend is reachable

If any of those fail, stop and recover the environment before diagnosing flow-specific issues.

For a repeatable capture path, collect a bundle first:

```bash
bash scripts/pilot_collect_diagnostics.sh /tmp/aicoos-pilot-diagnostics
```

If the pilot admin password differs from the default seeded value, override:

```bash
PILOT_DIAGNOSTICS_USERNAME=admin \
PILOT_DIAGNOSTICS_PASSWORD='<current-password>' \
bash scripts/pilot_collect_diagnostics.sh /tmp/aicoos-pilot-diagnostics
```

## Request Correlation

### Backend

Backend responses now include:

- response header: `X-Request-Id`
- response body field: `requestId`

Use the backend `requestId` first when a browser or API action fails.

### Runtime

Runtime responses include:

- response header: `X-Request-Id`

Runtime diagnostics snapshots also include:

- `requestId`

### Outbound propagation

Spring Boot propagates `X-Request-Id` into:

- `agent_runtime`
- Linear calls
- Figma calls

This gives one practical correlation thread for pilot troubleshooting even without a full tracing stack.

## Diagnostics Endpoints

### Backend diagnostics

Endpoint:

- `GET /api/system/diagnostics`

Auth:

- admin-authenticated backend user

What it shows:

- systems such as `agent_runtime`, `linear`, and `figma`
- operation-level success and failure counts
- last duration and average duration
- last failure classification
- last failure request id
- last failure message

### Runtime diagnostics

Endpoint:

- `GET /health/diagnostics`

Auth:

- requires `X-Agent-Runtime-Key`

What it shows:

- per-path success and failure counts
- last and average duration
- last failure classification
- last failure request id

## Common Diagnostic Workflow

When a user reports a failure:

1. capture the backend `requestId`
2. check the backend diagnostics snapshot
3. if the failure involves runtime, check runtime diagnostics next
4. if the failure involves Linear or Figma, use the backend classification and message to narrow the cause
5. preserve the evidence in the current regression or verification run directory

## Reading Classifications

### `auth`

Meaning:

- credential mismatch
- missing token
- invalid token
- unauthorized access

Check next:

- environment override vs database config source
- runtime shared key pairing
- Linear/Figma key validity

### `payload_invalid`

Meaning:

- the request format or required fields are wrong

Check next:

- target IDs
- required fields
- whether the current client path matches the verified input format

### `target_not_found`

Meaning:

- the referenced path, issue, file, or node was not found

Check next:

- Linear issue id
- Figma file key or node id
- current project binding state

### `permission_or_team`

Meaning:

- the key is valid but the chosen team or workspace context is not accepted

Check next:

- `integration.linear.defaultTeamId`
- the API key's team/workspace access scope

### `upstream_unavailable`

Meaning:

- timeout
- 5xx
- service unavailable

Check next:

- provider health
- local network reachability
- retryable transient failure vs persistent outage

### `network`

Meaning:

- transport-level connection or timeout issue

Check next:

- address and port
- TLS or connectivity problems
- local environment reachability

## Practical Curl Examples

### Backend diagnostics

Use an authenticated session or token from your normal pilot environment.

Example:

```bash
curl -s http://127.0.0.1:8081/api/system/diagnostics \
  -H "Authorization: Bearer <backend-token>"
```

### Runtime diagnostics

```bash
curl -s http://127.0.0.1:8091/health/diagnostics \
  -H "X-Agent-Runtime-Key: <runtime-shared-key>"
```

## Logging Boundary

Diagnostics are intended to improve visibility without exposing secrets.

Current rule:

- use request ids, status, classification, and safe failure messages
- do not log secret values or authorization headers

## Current Limits

The current diagnostics model is intentionally lightweight:

- counters are in-memory
- timings are lightweight and non-historical
- no central metrics store exists
- no alerting exists
- diagnostics reset on process restart

## Bundle Contents

The diagnostics helper currently stores:

- `notes/compose-ps.txt`
- `logs/mysql.log`
- `logs/agent_runtime.log`
- `logs/backend.log`
- `logs/frontend.log`
- `api/backend-health.json`
- `api/runtime-health.json`
- `api/frontend-index.html`
- optional `api/auth-login.json`
- optional `api/backend-diagnostics.json`
- optional `api/runtime-diagnostics.json`

Use the bundle directory as the first artifact to attach to pilot troubleshooting notes.

Current safety rule for the helper:

- stored `auth-login.json` redacts access and refresh tokens before writing to disk

This is still materially better than ad hoc troubleshooting, but it is not a full observability platform.
