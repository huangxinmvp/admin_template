# Local Agent Runtime E2E Runbook

## Purpose

This runbook makes the M12 runtime integration locally runnable and manually verifiable without adding any new product features.

For the full local stack order and the broader M15 demo path, also use:

- [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
- [m15-operator-verification-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m15-operator-verification-checklist.md)

M12.5 keeps the boundary unchanged:

- `web_backend` is still the only frontend-facing system
- `agent_runtime` is suggestion-only
- high-impact writes still require explicit operator confirmation in the main system

## Local Services

- Spring Boot backend: `http://127.0.0.1:8081`
- agent runtime: `http://127.0.0.1:8091`
- frontend dev server: Umi local dev server from `web_frontend`

## Prerequisites

- Java 21
- Node.js 20+
- Python 3.10+
- local MySQL reachable by the backend

Backend defaults from [application.yml](/Users/hx/it_company/agent_company/web_backend/src/main/resources/application.yml):

- `DB_URL=jdbc:mysql://localhost:3306/admin_template?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- `DB_USERNAME=root`
- `DB_PASSWORD=aA123456`
- backend port `8081`

If your local DB differs, override the backend env vars before starting Spring Boot.

## Runtime Environment

Use [`.env.example`](/Users/hx/it_company/agent_company/agent_runtime/.env.example) as the reference.

Important environment variables:

- `AGENT_RUNTIME_API_KEY`
- `AGENT_RUNTIME_PROVIDER`
- `AGENT_RUNTIME_MODEL`
- `AGENT_RUNTIME_TIMEOUT_SECONDS`
- `AGENT_RUNTIME_ALLOW_MOCK_FALLBACK`
- `OPENAI_COMPATIBLE_BASE_URL`
- `OPENAI_COMPATIBLE_API_KEY`

The runtime also accepts these common compatibility env names as fallback:

- `OPENAI_BASE_URL`
- `OPENAI_API_KEY`

M15 recommendation:

- keep model-provider secrets in runtime environment variables only
- use Spring Boot system-config or env override only for the Spring Boot -> runtime shared key

## Spring Boot System Config Keys

These keys are read by the backend runtime client:

- `agentRuntime.enabled`
- `agentRuntime.baseUrl`
- `agentRuntime.apiKey`
- `agentRuntime.timeoutMs`
- `agentRuntime.provider`
- `agentRuntime.model`
- `agentRuntime.mockMode`

Recommended local mock-mode values:

- `agentRuntime.enabled=true`
- `agentRuntime.baseUrl=http://127.0.0.1:8091`
- `agentRuntime.apiKey=` blank is acceptable for local demo
- `agentRuntime.timeoutMs=5000`
- `agentRuntime.provider=mock`
- `agentRuntime.model=mock-suggestion-v1`
- `agentRuntime.mockMode=true`

The backend now supports environment override precedence:

- `AICOOS_CONFIG_AGENT_RUNTIME_ENABLED`
- `AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`
- `AICOOS_CONFIG_AGENT_RUNTIME_TIMEOUT_MS`
- `AICOOS_CONFIG_AGENT_RUNTIME_PROVIDER`
- `AICOOS_CONFIG_AGENT_RUNTIME_MODEL`
- `AICOOS_CONFIG_AGENT_RUNTIME_MOCK_MODE`

Precedence is `environment variable -> database system-config -> default value`.

Recommended local real-provider values:

- `agentRuntime.enabled=true`
- `agentRuntime.baseUrl=http://127.0.0.1:8091`
- `agentRuntime.apiKey=` keep blank or set to a local shared key
- `agentRuntime.timeoutMs=10000`
- `agentRuntime.provider=openai_compatible`
- `agentRuntime.model=<your provider model name>`
- `agentRuntime.mockMode=false`

## Config Hygiene Notes

- `agentRuntime.apiKey` is now treated as a password-style config in the config center.
- The config center does not re-display the stored value.
- Leaving that field blank while saving keeps the current stored value unchanged.
- For local demo, prefer leaving `agentRuntime.apiKey` blank and using the default local shared key `agent-runtime-local-key`.
- Do not store your real model-provider key in Spring Boot system config. Keep the provider key only in runtime env variables.
- Current limitation: if you do save `agentRuntime.apiKey` in `sys_system_config`, it is still stored in plaintext at rest in the database.

## Start agent_runtime In Mock Mode

From repo root:

```bash
python3 -m pip install -r agent_runtime/requirements.txt
bash scripts/start_agent_runtime_mock.sh
```

Expected health check:

```bash
curl -s http://127.0.0.1:8091/health
```

You should see:

- `status=ok`
- `provider=mock`
- `defaultProviderReady=true`
- `runtimeKeyConfigured=true`

Optional direct smoke verification:

```bash
bash agent_runtime/scripts/runtime_smoke_test.sh
```

## Start agent_runtime In Real Provider Mode

From repo root:

```bash
python3 -m pip install -r agent_runtime/requirements.txt
export AGENT_RUNTIME_API_KEY=agent-runtime-local-key
export AGENT_RUNTIME_PROVIDER=openai_compatible
export AGENT_RUNTIME_MODEL=<your-model>
export AGENT_RUNTIME_ALLOW_MOCK_FALLBACK=false
export OPENAI_COMPATIBLE_BASE_URL=<your-openai-compatible-base-url>
export OPENAI_COMPATIBLE_API_KEY=<your-provider-key>
python3 -m uvicorn agent_runtime.app:app --host 127.0.0.1 --port 8091
```

Expected health check:

```bash
curl -s http://127.0.0.1:8091/health
```

You should see:

- `provider=openai_compatible`
- `openaiCompatibleConfigured=true`
- `defaultProviderReady=true`
- `mockFallbackEnabled=false`

If `defaultProviderReady=false`, stop and fix the runtime env before testing the UI.

## Start Spring Boot

From repo root:

```bash
bash scripts/start_backend_local.sh
```

What to verify during startup:

- backend starts on port `8081`
- bootstrap SQL completes successfully
- no runtime-config exception appears during startup

## Start Frontend

From [web_frontend](/Users/hx/it_company/agent_company/web_frontend):

```bash
npm install
npm run start:dev
```

Optional stack preflight from repo root:

```bash
bash scripts/local_stack_preflight.sh
```

## Login And Basic Health Verification

If your local seed data is loaded, the default admin user is defined in [seed-saas-admin.sql](/Users/hx/it_company/agent_company/web_backend/src/main/resources/static/sql/seed-saas-admin.sql):

- username: `admin`
- password: `Admin@123456`

Minimum checks before UI demo:

1. Open `/system/config-center` and confirm the `Agent Runtime` group is present.
2. Confirm `agentRuntime.baseUrl` points to `http://127.0.0.1:8091`.
3. Confirm `agentRuntime.mockMode` matches the runtime mode you started.
4. Confirm `agentRuntime.apiKey` field is blank/masked instead of re-displaying the current value.

## Minimal Demo Flow

Recommended order:

1. Open `AICoOS -> 需求接收`
2. Pick or create a project with saved intake content
3. Click `生成澄清建议`
4. Preview suggestions and apply selected items
5. Open `AICoOS -> 澄清中心`
6. Click `AI 分析转决策`
7. Preview decision suggestions and apply selected items
8. Open `AICoOS -> 决策中心`
9. Open one decision and click `预算影响建议`
10. Preview and apply the suggestion
11. Open `AICoOS -> 项目中心`
12. Open project detail and use `会议总结`
13. Preview and save the generated summary

Use the companion checklist in [m12-e2e-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m12-e2e-checklist.md) while walking the flow.

## Fast Troubleshooting

- Backend says `Agent Runtime 未启用`
  - check `agentRuntime.enabled=true`
- Backend says `Agent Runtime 响应异常: HTTP 401`
  - backend `agentRuntime.apiKey` and runtime `AGENT_RUNTIME_API_KEY` do not match
- Runtime health shows `defaultProviderReady=false`
  - the runtime provider env is incomplete
- Real provider mode still returns mock-like behavior
  - check `agentRuntime.mockMode=false`
  - check runtime `AGENT_RUNTIME_ALLOW_MOCK_FALLBACK=false`
- Preview works but nothing persists
  - confirm you clicked the explicit apply/save action; preview endpoints do not write records by design
