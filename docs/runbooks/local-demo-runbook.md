# Local Demo Runbook

## Goal

Make the current AICoOS stack locally runnable in a repeatable order for demo and verification:

1. MySQL
2. `agent_runtime`
3. Spring Boot backend
4. frontend dev server
5. browser walkthrough

This runbook stays inside the current product scope. It does not add autonomous execution, background workers, or bidirectional sync.

## Service Topology

- MySQL: `127.0.0.1:3306`
- Spring Boot backend: `127.0.0.1:8081`
- `agent_runtime`: `127.0.0.1:8091`
- frontend dev server: local Umi dev server

## Prerequisites

- Docker or a reachable MySQL 8.x instance
- Java 21
- Node.js 20+
- Python 3.10+
- `npm install` already completed in [web_frontend](/Users/hx/it_company/agent_company/web_frontend)
- `python3 -m pip install -r agent_runtime/requirements.txt` already completed once

## Recommended Startup Order

### 1. Start MySQL

From repo root:

```bash
docker compose -f docker-compose.local.yml up -d mysql
```

Default local DB assumptions:

- host: `127.0.0.1`
- port: `3306`
- database: `admin_template`
- username: `root`
- password: `aA123456`

### 2. Start `agent_runtime`

Mock mode is the recommended first-pass demo mode:

```bash
bash scripts/start_agent_runtime_mock.sh
```

Real provider mode is supported, but validate mock mode first. Provider env is documented in:

- [local-agent-runtime-e2e.md](/Users/hx/it_company/agent_company/docs/runbooks/local-agent-runtime-e2e.md)

### 3. Start Spring Boot

From repo root:

```bash
bash scripts/start_backend_local.sh
```

The helper script aligns backend defaults with [docker-compose.local.yml](/Users/hx/it_company/agent_company/docker-compose.local.yml) and disables devtools restart noise for local verification.

### 4. Start Frontend

From [web_frontend](/Users/hx/it_company/agent_company/web_frontend):

```bash
npm run start:dev
```

### 5. Run Local Preflight

From repo root:

```bash
bash scripts/local_stack_preflight.sh
```

Expected result:

- MySQL port reachable
- `agent_runtime /health` reachable
- Spring Boot `/api/system/health` reachable

## Config Precedence

Current precedence for system-config-backed keys is:

1. environment override
2. database config
3. code default

Use environment override for sensitive or environment-specific values whenever possible.

Important env override examples:

- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`
- `AICOOS_CONFIG_AGENT_RUNTIME_TIMEOUT_MS`
- `AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY`
- `AICOOS_CONFIG_INTEGRATION_LINEAR_TIMEOUT_MS`
- `AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY`
- `AICOOS_CONFIG_INTEGRATION_FIGMA_TIMEOUT_MS`

## Minimal Repeatable Browser Demo Path

### Login and shell sanity

1. Open the frontend in browser
2. Log in as admin
3. Open `工作台 -> 数据概览`
4. Open `AICoOS -> 项目中心`
5. Confirm both pages load without obvious API or layout errors

### Runtime-assisted preview/apply path

1. Open `AICoOS -> 需求接收`
2. Run `生成澄清建议`
3. Preview and apply selected suggestions
4. Open `AICoOS -> 澄清中心`
5. Run `AI 分析转决策`
6. Preview and apply selected suggestions
7. Open `AICoOS -> 决策中心`
8. Open one decision and run `预算影响建议`
9. Preview and apply
10. Open `AICoOS -> 项目中心`
11. Open one project detail drawer and run `会议总结`
12. Preview and save

### External tool verification path

1. In `项目中心`, open `Linear 映射`
2. Run preview before apply
3. Apply only after preview looks correct
4. In the same project, open `Figma 上下文`
5. Read preview and confirm binding only if the resolved file/node is correct

### Collaboration preview path

1. In `需求接收` or `澄清中心`, open the requirement-clarification collaboration entry
2. In `决策中心`, open the decision-budget collaboration entry
3. In `项目中心`, open the product-architecture brief entry
4. For all three, confirm the operator sees a preview before any write action

## Companion Checklists

- [m15-operator-verification-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m15-operator-verification-checklist.md)
- [m12-e2e-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m12-e2e-checklist.md)
- [m13-tool-integration-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m13-tool-integration-checklist.md)

## Current Demo Limits

- No real-time streaming
- No background workers
- No bidirectional external sync
- No autonomous external writes
- No production-grade secret manager
