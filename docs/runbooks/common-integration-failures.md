# Common Integration Failures

## Purpose

This guide helps operators distinguish common runtime and external integration failures quickly during pilot verification or demo operation.

## Runtime Failures

### `Agent Runtime 鉴权失败，请检查 agentRuntime.apiKey 与运行时共享密钥是否一致`

Meaning:

- Spring Boot and `agent_runtime` do not agree on the shared key

Check:

- `AGENT_RUNTIME_API_KEY`
- `AICOOS_CONFIG_AGENT_RUNTIME_API_KEY`
- fallback `agentRuntime.apiKey`

### `Agent Runtime 请求或载荷无效，请检查请求结构、必填字段和当前运行时版本`

Meaning:

- request body does not match what the runtime expects

Check:

- required fields in the triggering flow
- current backend/runtime version alignment

### `Agent Runtime 当前不可用，请稍后重试并检查运行时健康状态`

Meaning:

- runtime returned a retryable or 5xx failure

Check:

- `http://127.0.0.1:8091/health`
- runtime logs
- current provider readiness

### `Agent Runtime 超时或网络不可达，请检查运行时地址、端口和健康状态`

Meaning:

- backend could not reliably reach runtime

Check:

- `agentRuntime.baseUrl`
- local port reachability
- runtime process status

## Linear Failures

### `Linear 鉴权失败，请检查 integration.linear.apiKey 是否有效且具备目标 workspace 的访问权限`

Meaning:

- Linear key is invalid or lacks workspace access

Check:

- `integration.linear.apiKey`
- environment override vs database source
- current workspace and key scope

### `Linear Team 无法识别，请检查 integration.linear.defaultTeamId 或当前弹窗中的 Team ID`

Meaning:

- the team id is missing, wrong, or not available to the key

Check:

- `integration.linear.defaultTeamId`
- team id entered in the UI
- whether the key can create issues in that team

### `Linear 目标 Issue 无法识别，请先确认项目主工作项映射存在且 externalId 是真实 Linear issue id`

Meaning:

- comment mode is pointing at an invalid or placeholder issue target

Check:

- project primary issue binding
- current `externalId`
- whether the bound issue is a real Linear internal issue id

### `Linear 请求参数无效，请检查当前 payload、Team ID、Issue ID 和字段格式`

Meaning:

- the request reached Linear but was rejected as invalid input

Check:

- title and description presence for issue creation
- issue id validity for comment creation
- current project mapping state

## Figma Failures

### `Figma 鉴权失败，请检查 integration.figma.apiKey 是否有效，并确认该 Token 有目标文件的读取权限`

Meaning:

- the token is invalid or cannot access the target file

Check:

- `integration.figma.apiKey`
- direct provider auth
- whether the file belongs to an accessible account or team

### `未找到目标 Figma 文件或节点，请检查链接、File Key 和 Node ID 是否正确`

Meaning:

- the token is valid enough to make a request, but the target file or node cannot be found

Check:

- file key
- node id
- whether the current input format is the verified one

### `未找到指定的 Figma 节点，请检查 node-id 是否正确，或确认当前 Token 对该文件有读取权限`

Meaning:

- file exists but the node lookup failed

Check:

- node id formatting
- whether `-` vs `:` is normalized correctly
- whether the node still exists in the current file version

### `Figma File Key 未提供`

Meaning:

- request did not resolve any usable target

Check:

- whether you passed `fileKey`
- whether the current `figmaUrl` parsing path is known-good for the provided link

## Config And Source Confusion

### UI shows blank password field

Meaning:

- this does not necessarily mean the value is absent
- password-style fields are intentionally masked and not re-displayed

Check:

- `configured`
- `valueSource`
- `environmentOverride`

### Value behaves differently than the UI suggests

Meaning:

- environment override may be winning over database config

Check:

- current precedence is `environment -> database -> default`
- verify whether the key is injected via environment variable

## Fast Triage Order

When a pilot operator sees a failure:

1. capture the backend `requestId`
2. read the error message exactly
3. check backend diagnostics
4. if runtime is involved, check runtime diagnostics
5. classify as:
   - auth
   - payload_invalid
   - target_not_found
   - permission_or_team
   - upstream_unavailable
   - network
6. follow the corresponding credential, target, or environment checks

## Boundary Reminder

These guides improve troubleshooting materially, but they are still part of a lightweight pilot-readiness model.

They do not replace:

- centralized logs
- persistent metrics
- alerting
- full distributed tracing
