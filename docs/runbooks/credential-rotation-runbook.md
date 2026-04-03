# Credential Rotation Runbook

## Goal

Provide a practical rotation and expiration handling guide for the current AICoOS pilot environment without requiring a full secret manager.

This runbook covers:

- Spring Boot -> `agent_runtime` shared key
- runtime provider credentials
- Linear API key
- Figma API key

This runbook does not claim production-grade secret management. It is a pilot-operations runbook.

## Pilot Baseline Placement

Current P3-M1 reference files:

- [deploy/pilot/compose.env.example](/Users/hx/it_company/agent_company/deploy/pilot/compose.env.example)
- [deploy/pilot/backend.env.example](/Users/hx/it_company/agent_company/deploy/pilot/backend.env.example)
- [deploy/pilot/agent_runtime.env.example](/Users/hx/it_company/agent_company/deploy/pilot/agent_runtime.env.example)

Recommended pilot practice:

- copy the examples to non-git env files
- restrict those env files to owner-only permissions such as `chmod 600`
- keep Compose deployment wiring in `deploy/pilot/compose.env`
- keep application secrets in `deploy/pilot/backend.env` and `deploy/pilot/agent_runtime.env`
- rotate secrets in those env files first
- keep database fallback only where compatibility requires it

## Rotation Principles

### Environment-variable-first

Prefer environment variables for sensitive values whenever the deployment path allows it.

Reason:

- backend precedence already supports environment override
- environment variables avoid routine operator edits of password-style values in the config center
- this reduces accidental persistence of pilot secrets in database-backed system-config

### Keep UI masking behavior

If a password-style value must be present in system-config:

- the UI should remain masked
- saving a blank value should preserve the existing stored value
- operators should not expect the UI to re-display the secret

### Do not log secrets

During rotation or troubleshooting:

- do not paste secret values into tickets, comments, or documents
- do not add secret values to shell history intentionally
- do not add debug logging that prints tokens, API keys, Authorization headers, or runtime shared keys

## Config Placement Guide

### Best current placement

#### Keep in environment variables

- `DB_PASSWORD`
- runtime provider keys such as `OPENAI_COMPATIBLE_API_KEY` and `OPENAI_API_KEY`
- `AGENT_RUNTIME_API_KEY`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`
- `AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY`
- `AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY`

#### Safe to keep in database system-config

- `agentRuntime.enabled`
- `agentRuntime.baseUrl`
- `agentRuntime.timeoutMs`
- `agentRuntime.provider`
- `agentRuntime.model`
- `agentRuntime.mockMode`
- `integration.linear.enabled`
- `integration.linear.graphqlUrl`
- `integration.linear.defaultTeamId`
- `integration.linear.timeoutMs`
- `integration.figma.enabled`
- `integration.figma.apiBaseUrl`
- `integration.figma.timeoutMs`

### Current limitation

Password-style values stored in `sys_system_config` are masked in UI but still stored in the database.

That is acceptable for controlled demo and pilot operations with discipline, but it is not equivalent to:

- a vault
- KMS-backed storage
- secret versioning or automatic expiry controls

## Rotation Playbook By Credential Type

### 1. Spring Boot to Runtime Shared Key

Relevant keys:

- runtime env: `AGENT_RUNTIME_API_KEY`
- backend env override: `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`
- backend fallback system-config key: `agentRuntime.apiKey`

Preferred pilot rotation path:

1. generate a new shared key
2. update `AGENT_RUNTIME_API_KEY` in the runtime environment or `deploy/pilot/agent_runtime.env`
3. update `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY` in the backend environment or `deploy/pilot/backend.env`
4. restart `agent_runtime`
5. restart Spring Boot
6. verify runtime-backed preview flows

Verification:

- `curl -s http://127.0.0.1:8091/health`
- confirm one runtime-backed preview flow still works
- if backend reports `Agent Runtime 响应异常: HTTP 401`, the two sides are out of sync
- run `bash scripts/pilot_env_audit.sh` after updating both env files

Rollback:

- restore the previous shared key to both environments
- restart runtime and backend

### 2. Runtime Provider Credentials

Relevant keys:

- `OPENAI_COMPATIBLE_API_KEY`
- `OPENAI_API_KEY`
- `OPENAI_COMPATIBLE_BASE_URL`
- `OPENAI_BASE_URL`
- `AGENT_RUNTIME_PROVIDER`
- `AGENT_RUNTIME_MODEL`

Preferred rotation path:

1. update provider key in runtime environment only
2. keep Spring Boot config unchanged unless provider routing behavior also changes
3. restart `agent_runtime`
4. verify `agent_runtime /health`
5. verify one runtime-backed preview flow

Verification:

- `curl -s http://127.0.0.1:8091/health`
- confirm `defaultProviderReady=true`
- confirm one suggestion flow still responds correctly
- run `bash scripts/pilot_env_audit.sh` if provider config was changed in env files

Rollback:

- restore the prior provider env values
- restart `agent_runtime`

### 3. Linear API Key

Relevant keys:

- preferred: `AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY`
- fallback system-config key: `integration.linear.apiKey`

Preferred pilot rotation path:

1. create a replacement Linear personal API key with the minimum required permissions
2. if possible, inject it via `AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY`
3. keep `integration.linear.enabled`, `integration.linear.graphqlUrl`, and `integration.linear.defaultTeamId` unchanged unless the workspace or team changes
4. restart Spring Boot if environment variables changed
5. verify one real `preview -> apply` project mapping flow
6. verify one real comment flow if project mapping is already present
7. revoke the old key in Linear after verification succeeds

Verification:

- project mapping preview succeeds
- project mapping apply succeeds
- comment preview/apply succeeds against a real bound issue
- run `bash scripts/pilot_env_audit.sh` if the key was injected via env files

Rollback:

- re-inject the previous valid key
- restart Spring Boot if environment variables changed

### 4. Figma API Key

Relevant keys:

- preferred: `AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY`
- fallback system-config key: `integration.figma.apiKey`

Preferred pilot rotation path:

1. create or issue a replacement Figma token
2. if possible, inject it via `AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY`
3. keep `integration.figma.enabled` and `integration.figma.apiBaseUrl` unchanged unless the endpoint changes
4. restart Spring Boot if environment variables changed
5. verify direct provider access to a known real file or node
6. verify one real `preview -> apply` Figma bind flow
7. revoke the old key only after the new flow verifies cleanly

Verification:

- one known real target preview succeeds
- one bind/apply succeeds
- project binding and integration audit update correctly
- run `bash scripts/pilot_env_audit.sh` if the token was injected via env files

Rollback:

- restore the prior valid token
- restart Spring Boot if environment variables changed

## Expiration Handling

For pilot operation, maintain a simple manual register for:

- owner of each credential
- where it is injected
- when it was last rotated
- expected expiry or review date

Minimum recommendation:

- review all pilot credentials before each formal pilot session
- rotate any credential that is temporary, shared too broadly, or exposed in ad hoc troubleshooting
- record which env file or deployment variable was updated before restarting the pilot stack

## Verification Checklist After Any Rotation

1. backend starts cleanly
2. `agent_runtime` health is reachable if runtime is in scope
3. config center still masks password-style values
4. no secret value is echoed in operator-facing logs or notes
5. one real flow per affected integration succeeds
6. old credential is revoked only after new credential verification succeeds

## Current Limits

This runbook does not provide:

- secret version history
- automatic rotation
- expiry alarms
- secret-at-rest encryption guarantees beyond the current application/database behavior
- privileged secret segregation by environment platform

These remain follow-up concerns beyond P2A-M2.
