# Figma Integration Verification

## Goal
Verify one real `context read -> project binding` flow against an actual Figma file or node.

## Required Config
In `系统管理 -> 系统配置 -> 工具集成` set:
- `integration.figma.enabled=true`
- `integration.figma.apiBaseUrl=https://api.figma.com/v1`
- `integration.figma.apiKey=<real Figma API token>`
- `integration.figma.timeoutMs=10000`

Notes:
- API Key is masked in UI.
- The value is still stored in the current system-config table and is not a production-grade secret manager.
- Prefer env override for real verification:
  - `AICOOS_CONFIG_INTEGRATION_FIGMA_ENABLED=true`
  - `AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY=<real Figma API token>`
  - `AICOOS_CONFIG_INTEGRATION_FIGMA_TIMEOUT_MS=10000`

## Minimum Real Verification Path
1. Open `AICoOS -> 项目中心`
2. Open one project detail drawer
3. Click `Figma 上下文`
4. Paste one real Figma URL
   - preferred: full `https://www.figma.com/design/...?...node-id=...`
   - fallback: `fileKey` plus optional `nodeId`
5. Click `读取预览`
6. Confirm that preview resolves:
   - binding type
   - file name
   - optional node name
   - external link
7. Click `确认关联`
8. Confirm that:
   - the binding appears in `外部工具上下文`
   - the audit appears in `最近集成动作`
   - recent activity shows the integration event

## Expected Operator Behavior
- Figma preview is read-only
- Apply only stores the linkage in AICoOS
- No content is written back into Figma
- Node-level reads should fail clearly when the node is wrong or inaccessible
- The operator should be able to close preview without changing any project-side binding

## Common Failure Messages
- `Figma 集成未启用...`
  Action: enable `integration.figma.enabled`
- `Figma API Key 未配置...`
  Action: set `integration.figma.apiKey`
- `Figma 鉴权失败...`
  Action: verify token validity and file access scope
- `未找到目标 Figma 文件或节点...`
  Action: re-check URL, file key, node id
- `未找到指定的 Figma 节点...`
  Action: verify node-id and token access to that file

## Known Limitations
- No writeback to Figma
- No comment sync
- No design diffing
- No stage transition automation based on Figma context
