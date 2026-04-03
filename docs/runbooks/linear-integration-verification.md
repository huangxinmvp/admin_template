# Linear Integration Verification

## Goal
Verify one real, controlled `preview -> apply` flow against an actual Linear workspace without enabling autonomous writes.

## Required Config
In `系统管理 -> 系统配置 -> 工具集成` set:
- `integration.linear.enabled=true`
- `integration.linear.graphqlUrl=https://api.linear.app/graphql`
- `integration.linear.apiKey=<real Linear API key>`
- `integration.linear.defaultTeamId=<team id>` if you want Issue creation to work without filling Team ID every time
- `integration.linear.timeoutMs=10000`

Notes:
- API Key is masked in UI.
- The value is still stored in the current system-config table and is not a production-grade secret manager.
- Prefer env override for real verification:
  - `AICOOS_CONFIG_INTEGRATION_LINEAR_ENABLED=true`
  - `AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY=<real Linear API key>`
  - `AICOOS_CONFIG_INTEGRATION_LINEAR_DEFAULT_TEAM_ID=<team id>`
  - `AICOOS_CONFIG_INTEGRATION_LINEAR_TIMEOUT_MS=10000`

## Minimum Real Verification Path

### Path A: project mapping
1. Open `AICoOS -> 项目中心`
2. Open one project detail drawer
3. Click `Linear 映射`
4. Choose `创建 Linear 主工作项`
5. Optionally fill Team ID, title, description
6. Click `预览映射`
7. Confirm that preview text matches the intended project context
8. Click `确认映射`
9. Confirm that:
   - the new binding appears in `外部工具上下文`
   - one success audit appears in `最近集成动作`
   - recent activity contains an integration item

### Path B: clarification or decision export
Prerequisite:
- the project already has a `Linear 主工作项` binding if you want to use Comment mode

Clarification path:
1. Open `AICoOS -> 澄清中心`
2. Select the project
3. Open a row action `Linear`
4. Choose `写入 Linear Comment` or `写入 Linear Issue`
5. Click `预览写入`
6. Check the preview body and target
7. Click `确认写入`

Decision path:
1. Open `AICoOS -> 决策中心`
2. Open one decision detail drawer
3. Click `Linear 导出`
4. Repeat the same preview/apply validation

## Expected Operator Behavior
- Preview must succeed before apply is enabled
- Apply is always explicit
- Every apply must create a success or failure audit
- Comment mode should fail clearly when the project has no primary Linear issue mapping
- The operator should be able to stop after preview without any external write happening

## Common Failure Messages
- `Linear 集成未启用...`
  Action: enable `integration.linear.enabled`
- `Linear API Key 未配置...`
  Action: set `integration.linear.apiKey`
- `Linear Team ID 未配置...`
  Action: set `integration.linear.defaultTeamId` or enter Team ID in modal
- `当前项目还没有可用于评论的 Linear 主工作项...`
  Action: create/link a project-level Linear mapping first, or switch to Issue mode
- `Linear 鉴权失败...`
  Action: check API Key scope and workspace access

## Known Limitations
- No bidirectional sync
- No background sync
- No automatic issue state updates back into AICoOS
- No notifications after successful write
- No per-tenant secret manager yet
