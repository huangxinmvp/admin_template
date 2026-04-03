# Production Hardening

## Scope

M15 does not add new product capabilities. It hardens the existing platform so the current AICoOS feature set is easier to start, verify, demo, and prepare for real deployment.

## What M15 Tightens

### Local startup reliability

- local MySQL bootstrap stays aligned with [docker-compose.local.yml](/Users/hx/it_company/agent_company/docker-compose.local.yml)
- [start_backend_local.sh](/Users/hx/it_company/agent_company/scripts/start_backend_local.sh) reduces backend startup friction by aligning Spring Boot DB defaults with the local compose defaults
- [start_agent_runtime_mock.sh](/Users/hx/it_company/agent_company/scripts/start_agent_runtime_mock.sh) remains the fastest path for a controlled demo
- [local_stack_preflight.sh](/Users/hx/it_company/agent_company/scripts/local_stack_preflight.sh) provides one quick stack readiness check

### External/runtime reliability guardrails

- runtime, Linear, and Figma outbound calls now carry request correlation IDs
- runtime, Linear, and Figma calls have explicit timeout handling
- transient timeout/network/service-unavailable conditions get one lightweight retry
- common auth/mapping/path failures now surface clearer operator-facing messages

### Config and secret hygiene

- system-config now supports environment override precedence
- config-center shows whether a value comes from environment, database, or default
- password-style values remain masked in UI
- secrets are intentionally kept out of logs in the current hardening scope

## Current Config Precedence

1. environment override
2. database system-config
3. code default

Use env override for environment-specific endpoints and secrets whenever possible.

## Key Runbooks

- [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
- [local-agent-runtime-e2e.md](/Users/hx/it_company/agent_company/docs/runbooks/local-agent-runtime-e2e.md)
- [linear-integration-verification.md](/Users/hx/it_company/agent_company/docs/runbooks/linear-integration-verification.md)
- [figma-integration-verification.md](/Users/hx/it_company/agent_company/docs/runbooks/figma-integration-verification.md)
- [m15-operator-verification-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m15-operator-verification-checklist.md)
- [deployment-preparation.md](/Users/hx/it_company/agent_company/docs/runbooks/deployment-preparation.md)

## What M15 Does Not Solve

- no full secret-manager or vault integration
- no background workers or autonomous execution
- no bidirectional external sync
- no real-time streaming or advanced observability stack
- no deployment manifests or HA topology

## Readiness Boundary

After M15, the platform is in a better state for:

- repeatable local demo
- operator walkthrough
- integration verification
- incremental deployment preparation

It is still not equivalent to full production operations maturity. Secrets at rest, runtime supervision, metrics, alerting, and deployment automation remain follow-up concerns.
