# Linear 400 Root Cause Report

## Scope

This report captures the focused M13.6 Linear 400 debugging pass.

This pass stayed within the existing AICoOS scope:

- no new product features
- no Figma work
- no workflow redesign
- only one debugging-focused backend clarity improvement

## Evidence Reviewed

Primary recorded evidence:

- `/tmp/aicoos-m15-check/linear-rerun-project-preview.json`
- `/tmp/aicoos-m15-check/linear-rerun-project-apply.json`
- `/tmp/aicoos-m15-check/linear-rerun-decision-comment-preview.json`
- `/tmp/aicoos-m15-check/linear-rerun-decision-comment-apply.json`
- `/tmp/aicoos-m15-check/linear-rerun-audits.json`

Supporting runtime observations:

- project mapping preview succeeds
- project mapping apply fails with `Linear 响应异常: HTTP 400`
- decision comment preview succeeds
- decision comment apply fails with `Linear 响应异常: HTTP 400`
- current demo primary issue binding for `demo_project_alpha` still resolves to placeholder target `LIN-EXTERNAL-001`

Additional direct verification performed in this pass:

- read-only direct Linear GraphQL probe against `https://api.linear.app/graphql`
- `Authorization: Bearer <apiKey>` returned HTTP 400 with provider message:
  - `It looks like you're trying to use an API key as a Bearer token. Remove the Bearer prefix from the Authorization header.`
- `Authorization: <apiKey>` returned HTTP 200 and successfully resolved `viewer`

## Current Implementation Review

Current backend client:

- [LinearIntegrationClientImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/integration/LinearIntegrationClientImpl.java)

Relevant implementation detail:

- `resolveConfig()` currently rewrites any configured API key into `Bearer <apiKey>` unless it already starts with `Bearer `

Current apply payloads:

### Project mapping apply

Recorded request:

- `teamId=83633b9b-b0c9-45c2-9b2d-3c01d8d673ce`
- title and description are both present

Conclusion:

- the project mapping payload shape itself is not the likely primary problem
- the issue creation request includes the minimum fields expected by current implementation: `teamId`, `title`, `description`

### Decision comment apply

Recorded preview result:

- comment mode resolves target issue id to `LIN-EXTERNAL-001`
- that value comes from the current project primary binding, not from a real provider lookup

Conclusion:

- comment-mode preview is polluted by a placeholder binding
- even after the main auth bug is fixed, comment apply is still likely to fail until the project mapping is replaced with a real Linear issue id

## Root Cause Separation

### 1. Payload / implementation issues

Most likely root cause for the current project mapping apply failure:

- **implementation bug in Linear auth header handling**
- the GraphQL client sends `Authorization: Bearer <apiKey>`
- the direct provider probe showed that Linear's GraphQL endpoint rejects this exact form for personal API keys with HTTP 400

Why this is the strongest root cause:

- the provider returned a specific error message naming the Bearer-prefix misuse
- the same key succeeds in a direct read-only GraphQL call when sent as raw `Authorization: <apiKey>`
- project mapping preview already proves the local AICoOS-side payload assembly is coherent before provider invocation

### 2. Team / workspace permission issues

This is **not the most likely primary blocker** for the current project mapping apply failure.

Reason:

- the direct read-only provider probe succeeded with the same API key when the Authorization header was sent correctly
- this confirms the key is at least valid enough to authenticate against the workspace

Residual uncertainty:

- after the auth-header bug is fixed, issue-creation permissions for the chosen team still need one real re-check
- but current evidence says auth-header formatting is the first blocker, not team/workspace membership

### 3. Invalid target issue binding issues

This is **definitely a blocker for comment mode**, but **not the primary root cause of the current project mapping apply failure**.

Reason:

- decision comment preview resolves `targetIssueId=LIN-EXTERNAL-001`
- that is a placeholder binding value, not a verified real Linear internal issue id
- even with a corrected auth header, comment apply is still likely to fail against that placeholder target

## Are The Two Apply Failures The Same Issue?

They are the **same immediate issue**, but **not the same full end-state issue**.

### Same immediate blocker

Both current failures are presently blocked by the same implementation problem:

- the backend sends the API key with a `Bearer ` prefix to Linear GraphQL

### Different downstream blockers

After that auth-header issue is fixed:

- project mapping apply should become the cleanest first re-test candidate
- decision comment apply will still likely remain blocked by the placeholder project primary issue binding `LIN-EXTERNAL-001`

So the clean separation is:

- **project mapping apply**: primarily blocked by auth-header implementation
- **comment apply**: currently blocked by auth-header implementation, and secondarily polluted by invalid placeholder issue binding

## Debugging-Focused Change Made

One minimal backend clarity improvement was made in:

- [LinearIntegrationClientImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/integration/LinearIntegrationClientImpl.java)
- [LinearIntegrationClientImplTest.java](/Users/hx/it_company/agent_company/web_backend/src/test/java/com/hiking/treasure/modules/phase1/integration/LinearIntegrationClientImplTest.java)

What changed:

- non-2xx Linear responses are now logged with response body for debugging
- HTTP 400 responses now parse GraphQL `errors[0].message` when present
- the specific Bearer-prefix misuse now maps to an actionable operator/developer message instead of generic `HTTP 400`
- one targeted test was added for this case

Validation:

- `./mvnw -q -Dtest=LinearIntegrationClientImplTest test` passed

## Most Likely Root Cause

The most likely root cause is:

- **AICoOS currently prepends `Bearer ` to personal Linear API keys when calling the Linear GraphQL API, and Linear rejects that header format with HTTP 400.**

## Smallest Next Fix To Try

The smallest next fix to try is:

1. stop auto-prepending `Bearer ` in [LinearIntegrationClientImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/integration/LinearIntegrationClientImpl.java) for GraphQL API-key auth
2. rerun only the project mapping `preview -> apply` flow first
3. only after that succeeds, replace the placeholder project primary binding with the newly created real Linear issue id and rerun decision comment mode

This sequencing keeps the next step tightly scoped and prevents comment-mode noise from hiding whether the main project-create path is fixed.
