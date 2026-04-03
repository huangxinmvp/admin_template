# AICoOS Demo Verification Report

## 1. Verification Scope

This report captures the latest manual verification round for the current AICoOS demo state.

This round validated:

- AICoOS internal governance flows through live Spring Boot APIs
- runtime suggestion preview/apply flows through the running `agent_runtime`
- M14 lightweight collaboration preview/apply entry points
- Project Center, Approval Center, and Dashboard aggregate linkage after governed writes
- Linear project-mapping preview/apply behavior inside AICoOS
- current blocked-state behavior for real Linear write and real Figma read paths

This round did **not** validate:

- real external Linear issue/comment creation against a live enabled workspace
- real Figma file/node read against a live enabled token
- browser-level visual walkthrough

Services running during verification:

- Spring Boot backend: `http://127.0.0.1:8081`
- `agent_runtime`: `http://127.0.0.1:8091`

Local endpoints used in this round:

- `POST /api/auth/login`
- `GET /api/aicoos/project/center/page`
- `GET /api/aicoos/project/center/{projectId}`
- `GET /api/aicoos/requirementIntake/project/{projectId}`
- `POST /api/aicoos/requirementIntake/project/{projectId}/clarification-suggestions`
- `POST /api/aicoos/requirementIntake/project/{projectId}/clarification-suggestions/apply`
- `POST /api/aicoos/clarificationItem/project/{projectId}/decision-suggestions`
- `POST /api/aicoos/clarificationItem/project/{projectId}/decision-suggestions/apply`
- `POST /api/aicoos/decisionItem/{decisionItemId}/budget-impact-suggestion`
- `POST /api/aicoos/decisionItem/{decisionItemId}/budget-impact-suggestion/apply`
- `GET /api/aicoos/approvalRecord/page`
- `GET /api/system/dashboard`
- `GET /api/aicoos/toolIntegration/project/{projectId}/bindings`
- `GET /api/aicoos/toolIntegration/project/{projectId}/audits`
- `POST /api/aicoos/toolIntegration/project/{projectId}/linear/representation/preview`
- `POST /api/aicoos/toolIntegration/project/{projectId}/linear/representation/apply`
- `POST /api/aicoos/toolIntegration/decision/{decisionItemId}/linear/preview`
- `POST /api/aicoos/toolIntegration/decision/{decisionItemId}/linear/apply`
- `POST /api/aicoos/toolIntegration/project/{projectId}/figma/context/preview`
- `POST /api/aicoos/requirementIntake/project/{projectId}/clarification-collaboration`
- `POST /api/aicoos/decisionItem/{decisionItemId}/decision-budget-review`
- `POST /api/aicoos/decisionItem/{decisionItemId}/decision-budget-review/apply`
- `POST /api/aicoos/meetingRecord/project/{projectId}/product-architecture-brief`

Temporary verification artifacts were stored under:

- `/tmp/aicoos-m15-check/`

Representative artifacts from this round:

- `/tmp/aicoos-m15-check/intake-delta.json`
- `/tmp/aicoos-m15-check/clarification-suggestions-delta.json`
- `/tmp/aicoos-m15-check/clarification-apply-delta.json`
- `/tmp/aicoos-m15-check/decision-suggestions-delta.json`
- `/tmp/aicoos-m15-check/decision-apply-delta.json`
- `/tmp/aicoos-m15-check/budget-suggestion-new-decision.json`
- `/tmp/aicoos-m15-check/budget-apply-new-decision.json`
- `/tmp/aicoos-m15-check/project-center-delta-after.json`
- `/tmp/aicoos-m15-check/dashboard.json`
- `/tmp/aicoos-m15-check/linear-project-preview.json`
- `/tmp/aicoos-m15-check/linear-project-apply.json`
- `/tmp/aicoos-m15-check/linear-project-apply-full.json`
- `/tmp/aicoos-m15-check/linear-decision-preview-2.json`
- `/tmp/aicoos-m15-check/linear-decision-apply-2.json`
- `/tmp/aicoos-m15-check/figma-preview.json`
- `/tmp/aicoos-m15-check/clarification-collab.json`
- `/tmp/aicoos-m15-check/decision-budget-review.json`
- `/tmp/aicoos-m15-check/decision-budget-review-apply.json`
- `/tmp/aicoos-m15-check/product-architecture-brief.json`

## 2. Verified Working Flows

### AICoOS internal governance flows

The following internal governance flows are currently runnable against the live local stack:

- requirement intake -> clarification suggestion -> apply
- clarification -> decision suggestion -> apply
- decision -> budget impact suggestion -> apply
- Project Center / Approval Center / Dashboard linkage after governed writes

Exact verified results from this round on `demo_project_delta`:

- requirement-intake clarification suggestion generated `确认验收标准`
- clarification suggestion apply created clarification item `2038804232538685442`
- clarification decision-suggestion generated `确认上线回滚要求`
- decision suggestion apply created decision item `2038804297223241729`
- budget impact suggestion returned `+5% ~ +15%`
- budget impact apply wrote `budgetImpactSummary` and `projectImpactSummary` back to the new decision record

### Runtime suggestion flows

The current suggestion-first runtime path is working with the local `agent_runtime` in mock mode.

Verified runnable runtime-assisted flows:

- clarification suggestion generation
- decision promotion suggestion generation
- budget impact suggestion generation

The main system remained the source of truth in this round:

- preview happened first
- explicit apply was required
- domain writes happened only through Spring Boot APIs

### M14 lightweight collaboration flows

The M14 lightweight collaboration surfaces are also currently runnable:

- requirement clarification collaboration
- decision-budget collaboration
- product-architecture brief

Exact verified results:

- requirement clarification collaboration returned blocker assessment and next questions
- decision-budget collaboration returned recommendation, budget note, and next steps
- decision-budget collaboration apply successfully updated the decision through the governed apply path
- product-architecture brief returned a structured brief, risks, and open questions

## 3. External-Tool Current Status

### Linear

Real external Linear write is currently blocked by local integration config.

Current verified state:

- project-side Linear representation `preview -> apply` is working inside AICoOS
- project binding replacement and audit recording are working inside AICoOS
- decision comment `preview` works only after a usable primary issue binding includes a real `existingIssueId`

Current blocker:

- real Linear write `apply` is blocked by `integration.linear.enabled`

Observed blocking error:

- `Linear 集成未启用，请在系统配置的工具集成分组打开 integration.linear.enabled`

### Figma

Real Figma context read is currently blocked by local integration config.

Current verified state:

- the AICoOS business-side preview endpoint is reachable
- blocked-state error handling is clear and actionable

Current blocker:

- real Figma context read is blocked by `integration.figma.enabled`

Observed blocking error:

- `Figma 集成未启用，请在系统配置的工具集成分组打开 integration.figma.enabled`

### Remaining prerequisites for true external-tool invocation

The remaining prerequisites are:

- enable `integration.linear.enabled`
- enable `integration.figma.enabled`
- provide valid Linear credentials and required mapping data
- provide valid Figma credentials and real file/node context

Until those are present, external-tool verification remains limited to preview/business-path behavior and blocked-state checks.

## 4. Real Issues Discovered

### Linear “link existing” operator friction

This round found a real operator usability gap around Linear existing-link flows.

Observed behavior:

- saving a project mapping with only `existingIssueIdentifier` and `existingIssueUrl` is not enough for later comment-mode usage
- decision comment mode depends on a usable project-level Linear primary issue binding
- that binding must include a real `existingIssueId`

Practical implication:

- operators can believe the project is already “mapped to Linear”
- but comment-mode export still fails unless the mapping contains `existingIssueId`

Observed error before the stronger mapping was applied:

- `当前项目还没有可用于评论的 Linear 主工作项。请先在项目中心建立 Linear 项目映射，或切换为 Issue 模式`

This is the main operator friction item preserved from this round.

## 5. Current Environment Snapshot

- Spring Boot: `http://127.0.0.1:8081`
- `agent_runtime`: `http://127.0.0.1:8091`
- temp verification artifacts path: `/tmp/aicoos-m15-check/`

Additional context from this round:

- validation was performed mainly through authenticated API calls
- this report does not claim browser-level walkthrough completion

## 6. Risks / Follow-Up Actions

### Configuration blockers

- `integration.linear.enabled` is still disabled in the current local config state
- `integration.figma.enabled` is still disabled in the current local config state
- real external-tool invocation cannot proceed until those flags are enabled

### Integration blockers

- Linear comment-mode export depends on a primary project mapping with `existingIssueId`
- real Linear invocation still needs valid credentials and enablement
- real Figma invocation still needs valid credentials and enablement

### Production-readiness gaps

- this round verified local demo behavior, not production deployment behavior
- secrets still rely on the current config path rather than a production-grade secret manager
- no production observability, rollout controls, or external-call monitoring were validated here
- no browser automation evidence was captured in this round

## 7. Recommended Next Step

The next validation round should focus on external-tool enablement:

1. enable local integration config for Linear and Figma
2. provide real verification credentials
3. rerun true external-tool verification for:
   - Linear preview -> apply -> real write
   - Figma preview -> real context read / binding
4. keep storing evidence under `/tmp/aicoos-m15-check/`

Recommended framing for the current demo state:

- internal governance flows are already runnable
- runtime suggestion flows are already runnable
- M14 lightweight collaboration flows are already runnable
- external-tool real invocation is the next enablement step, not the current verified state
