# M15 Operator Verification Checklist

## Stack Preflight

- [ ] MySQL is reachable on the configured host/port
- [ ] `agent_runtime` is running and `GET /health` returns healthy
- [ ] Spring Boot is running on the expected port
- [ ] frontend dev server or deployed frontend is reachable
- [ ] `bash scripts/local_stack_preflight.sh` passes

## Browser Shell Walkthrough

- [ ] Login page loads with branding
- [ ] Admin login succeeds
- [ ] `工作台 -> 数据概览` loads
- [ ] `AICoOS -> 项目中心` loads
- [ ] `AICoOS -> 需求接收` loads
- [ ] `AICoOS -> 澄清中心` loads
- [ ] `AICoOS -> 决策中心` loads

## Spring Boot -> Runtime Verification

- [ ] Requirement intake clarification generation returns preview before apply
- [ ] Clarification-to-decision analysis returns preview before apply
- [ ] Decision budget suggestion returns preview before apply
- [ ] Meeting summary returns preview before save
- [ ] Closing a preview does not persist high-impact data
- [ ] After apply/save, Project Center reflects the expected updated governance counts

## Linear Verification

- [ ] A Linear preview can be opened from Project Center, Clarification Center, or Decision Center
- [ ] Preview text and target are readable before apply
- [ ] Apply stays explicit
- [ ] Missing project mapping in Comment mode shows a clear actionable error
- [ ] Successful apply creates project audit context and recent activity

## Figma Verification

- [ ] A Figma preview can be opened from Project Center
- [ ] Wrong file key or node id shows a clear actionable error
- [ ] Preview resolves file name and optional node context when auth is correct
- [ ] Confirming preview stores project-side binding only
- [ ] Closing preview does not change any binding

## M14 Collaboration Entry Points

- [ ] Requirement clarification collaboration entry works from requirement intake or clarification center
- [ ] Decision-budget collaboration entry works from decision center
- [ ] Product-architecture brief entry works from project center
- [ ] All three show preview before any apply/save action
- [ ] Applying collaboration output only writes through existing governed paths

## Config Hygiene Checks

- [ ] Config center masks password-style keys
- [ ] `agentRuntime.apiKey` is not re-displayed after save
- [ ] Linear and Figma API keys are masked
- [ ] Config center clearly indicates env override when active
- [ ] Operators understand that current DB-backed config storage is not a production-grade secret manager

## Error And Audit Behavior

- [ ] Failed runtime or external-tool calls return one actionable message instead of raw stack noise
- [ ] Success apply actions remain auditable from project context
- [ ] Retryable transient failures do not expose secrets
- [ ] Request IDs are available in error responses where applicable

## Stop Conditions

Stop the demo and fix the environment before continuing if:

- [ ] backend cannot reach DB or Flowable bootstrap fails
- [ ] runtime health is unhealthy
- [ ] preview actions persist data before explicit confirmation
- [ ] external-tool apply actions occur without preview
- [ ] config UI exposes secrets directly
