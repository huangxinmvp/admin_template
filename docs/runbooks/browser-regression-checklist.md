# Browser Regression Checklist

## Goal

Provide one repeatable browser-level walkthrough for the current AICoOS pilot-critical flows.

Use this checklist for:

- pilot smoke verification
- pre-demo verification
- post-change browser regression walkthroughs

Pair this checklist with:

- [environment-config-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/environment-config-guide.md)
- [evidence-capture-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/evidence-capture-guide.md)

## Preconditions

Before opening the browser:

- backend is healthy at `http://127.0.0.1:8081`
- `agent_runtime` is healthy at `http://127.0.0.1:8091`
- frontend is reachable at `http://127.0.0.1:8000`
- required pilot/demo credentials are already configured
- one new evidence run directory has been created under `/tmp/aicoos-regression/<run-id>/`

Suggested helper commands:

```bash
RUN_DIR=$(scripts/create_regression_run.sh /tmp/aicoos-regression browser-regression local)
scripts/collect_regression_evidence.sh "$RUN_DIR"
```

## Suggested Artifact Minimum

For one regression run, capture at minimum:

- one shell-health note
- one screenshot for login/dashboard
- one screenshot per major flow after preview
- one notes file with pass/fail observations

If screenshot capture fails in the current environment, keep the run valid by preserving:

- the generated `*.capture-failed.txt` note
- API evidence under `api/`
- operator observations in `notes/run-summary.md`

## Flow 0: Stack And Login

- [ ] Open the frontend
- [ ] Confirm login page loads with expected branding
- [ ] Log in as admin
- [ ] Capture screenshot: `01-login-or-dashboard.png`
- [ ] Record note: login success or failure

## Flow 1: Dashboard Review

- [ ] Open `工作台 -> 数据概览`
- [ ] Confirm the page loads without obvious layout or API errors
- [ ] Confirm the main operational cards render
- [ ] Capture screenshot: `02-dashboard.png`
- [ ] Record note: counts and attention areas look plausible

## Flow 2: Project Center

- [ ] Open `AICoOS -> 项目中心`
- [ ] Confirm the list loads
- [ ] Confirm the `下一步` column is readable across at least 3 projects with different governance states
- [ ] Confirm the recommendation text is visually primary, while priority and actor stay secondary and compact
- [ ] Confirm long `下一步` or actor text truncates cleanly with tooltip support
- [ ] Open one project detail drawer
- [ ] Confirm the `下一步建议` card is visible near the top and understandable within a few seconds
- [ ] Confirm the card order is easy to scan:
  - overall judgment
  - hard blockers
  - primary action
  - follow-up actions
- [ ] Confirm the top-level `下一步` hint and the expanded primary action feel consistent instead of contradictory
- [ ] Confirm blocker text and reason summary are consistent with governance counts, gate status, or budget status
- [ ] Confirm the primary action clearly answers:
  - why to do it now
  - what blocker/risk it addresses
  - what likely changes after completion
- [ ] Confirm follow-up actions remain lighter than the main action and do not feel like duplicated paragraphs
- [ ] Confirm the target-module action lands on the correct module with project context preserved where expected
- [ ] Confirm `project-center` guidance does not show a misleading self-jump button
- [ ] Confirm governance summary, external context, and recent activity are still visible
- [ ] Capture screenshot: `03-project-center.png`
- [ ] Record note: action-chain readability, top-level/detail consistency, jump correctness, and any copy ambiguity

## Flow 3: Requirement Intake -> Clarification Suggestion

- [ ] Open `AICoOS -> 需求接收`
- [ ] Select a valid project
- [ ] Trigger `生成澄清建议`
- [ ] Confirm preview appears before any write
- [ ] Apply one selected suggestion
- [ ] Capture screenshot: `04-clarification-preview.png`
- [ ] Record note: preview fields and apply result

## Flow 4: Clarification -> Decision Suggestion

- [ ] Open `AICoOS -> 澄清中心`
- [ ] Trigger `AI 分析转决策`
- [ ] Confirm preview appears before any write
- [ ] Apply one selected suggestion
- [ ] Capture screenshot: `05-decision-preview.png`
- [ ] Record note: created decision visibility

## Flow 5: Decision -> Budget Suggestion

- [ ] Open `AICoOS -> 决策中心`
- [ ] Open one decision detail drawer
- [ ] Trigger `预算影响建议`
- [ ] Confirm preview appears before any write
- [ ] Apply the suggestion
- [ ] Capture screenshot: `06-budget-preview.png`
- [ ] Record note: summary fields updated after apply

## Flow 6: Approval Actions

- [ ] Open `AICoOS -> 审批中心`
- [ ] Open one actionable approval
- [ ] Take one explicit approval action if the environment is intended for mutable verification
- [ ] Confirm status or history updates
- [ ] Capture screenshot: `07-approval-action.png`
- [ ] Record note: approval action and resulting state

## Flow 7: Dashboard Follow-Through

- [ ] Return to `工作台 -> 数据概览`
- [ ] Confirm no obvious regression in counts or attention areas
- [ ] Capture screenshot: `08-dashboard-follow-through.png`
- [ ] Record note: whether aggregate behavior still looks coherent

## Flow 8: Linear Preview / Apply

- [ ] Return to `AICoOS -> 项目中心`
- [ ] Open `Linear 映射`
- [ ] Confirm preview is readable
- [ ] Run apply only if the current run is meant to validate mutable integration behavior
- [ ] Record the resulting external link or audit visibility
- [ ] Capture screenshot: `09-linear-preview-or-result.png`

## Flow 9: Figma Preview / Apply

- [ ] Open `Figma 上下文`
- [ ] Prefer full `figmaUrl` or `fileKey + nodeId`
- [ ] Confirm preview resolves file or node metadata
- [ ] Run apply only if the current run is meant to validate mutable integration behavior
- [ ] Confirm binding and audit visibility
- [ ] Capture screenshot: `10-figma-preview-or-result.png`

## Flow 10: M14 Collaboration Entry Points

- [ ] Requirement clarification collaboration entry opens and previews correctly
- [ ] Decision-budget collaboration entry opens and previews correctly
- [ ] Product-architecture brief entry opens and previews correctly
- [ ] Capture screenshot: `11-m14-collaboration.png`
- [ ] Record note: each entry shows preview before governed write paths

## Pass Criteria

A run passes when:

- login and shell pages load
- all preview-first flows still show preview before apply
- Linear and Figma verified paths still work in the current environment
- no obvious secret leakage appears in UI
- evidence files are stored under the run directory

## Stop Conditions

Stop the walkthrough and mark the run blocked if:

- backend or runtime health is down
- preview fails before the environment-specific preconditions are checked
- a flow writes before explicit confirmation
- secrets appear unmasked in UI
- the environment is too unstable to produce trustworthy evidence
