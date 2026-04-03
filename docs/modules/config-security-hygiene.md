# Config Security Hygiene

## Purpose

This document records the current intended security-hygiene model for runtime and external integration credentials in AICoOS.

This is not a vault design. It is the current pilot-ready handling model for:

- Spring Boot system-config
- environment overrides
- runtime environment variables
- UI masking behavior
- logging boundaries

## Current Model

### Precedence

For backend system-config-backed values, the current precedence is:

1. environment override
2. database system-config
3. code default

This is implemented in:

- [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)

### Security posture

The current model is:

- environment-variable-first for secrets
- masked UI for password-style config
- database system-config still allowed as a fallback storage path
- explicit acknowledgement that database-backed config is not a production-grade secret manager

## Environment-Variable-First Rule

Prefer environment variables for:

- DB passwords
- runtime provider API keys
- Spring Boot to runtime shared keys
- Linear API keys
- Figma API keys
- environment-specific service endpoints where secrecy or environment isolation matters

Use system-config for:

- toggles
- timeouts
- UI-facing defaults
- operator-visible non-secret values
- stable demo/pilot settings that operators may need to inspect

## UI Masking Behavior

Current config-center behavior:

- password-style fields render as masked inputs
- password-style values are not re-displayed after load
- saving a blank password-style field preserves the existing stored value
- the UI can show whether the effective value source is environment, database, or default

Relevant implementation:

- [SystemConfigServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/service/impl/SystemConfigServiceImpl.java)
- [index.tsx](/Users/hx/it_company/agent_company/web_frontend/src/pages/system/config-center/index.tsx)

Operational implication:

- the UI is appropriate for controlled demo and pilot operation
- operators should not use the UI as proof that a password-style value is absent merely because it displays blank

## Logging Hygiene Boundary

Current intended boundary:

- do not log secret values
- do not log Authorization header contents
- do not log runtime shared keys
- do not log password-style system-config values

Current implementation posture:

- outbound clients log request IDs, status codes, and selective provider error bodies when needed
- outbound clients do not log secret header values
- config-center UI does not re-display password-style values
- runtime health intentionally reports readiness without exposing provider secrets

Current implication:

- pilot troubleshooting should rely on status, provider messages, and request IDs
- future diagnostic work should preserve this boundary rather than widening logs casually

## Current Limits

The current model still has important limits:

- password-style values in `sys_system_config` are still stored in the database
- this is not vault-grade storage
- there is no automatic expiry tracking
- there is no automatic rotation support
- there is no access audit dedicated to secret reads
- there is no secret version history

This means the current model is:

- acceptable for controlled demo and pilot usage with discipline
- not sufficient to claim production-grade secret-management maturity

## Integration-Specific Guidance

### Agent Runtime

Preferred handling:

- keep model-provider secrets in runtime environment variables only
- keep Spring Boot `agentRuntime.apiKey` in environment override if secrecy matters

### Linear

Preferred handling:

- keep `integration.linear.apiKey` in environment override for pilot usage
- use system-config for enable flag, GraphQL URL, default team id, and timeout

### Figma

Preferred handling:

- keep `integration.figma.apiKey` in environment override for pilot usage
- use system-config for enable flag, API base URL, and timeout

## Practical Pilot Rule

For pilot environments:

- secrets should prefer environment injection
- system-config should describe and control the environment, not become the main secret store
- rotation should follow the operator runbook, not ad hoc edits

## Non-Goals

This module does not propose:

- a new vault
- KMS integration
- sealed secrets
- tenant-level secret-manager features
- a new product UI for secret lifecycle management

Those belong to future phases if required.
