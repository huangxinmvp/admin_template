# AICoOS Demo Script

## Goal

Run a concise, controlled walkthrough of the current AICoOS demo version without implying production autonomy or finished operational maturity.

## Audience And Timing

- Executive or stakeholder walkthrough: 12-15 minutes
- Product and engineering handoff walkthrough: 20-25 minutes

## Demo Story In One Sentence

"AICoOS turns delivery into a governed operating system where AI helps prepare decisions, but people still control scope, budget, permissions, and release-critical actions."

## Pre-Demo Checks

Before presenting:

1. Start MySQL.
2. Start `agent_runtime` in mock mode unless real-provider verification is part of the session.
3. Start Spring Boot.
4. Start the frontend dev server.
5. Run [local_stack_preflight.sh](/Users/hx/it_company/agent_company/scripts/local_stack_preflight.sh).
6. Confirm admin login works.
7. Confirm demo data exists for at least one project with intake, clarification, decision, approval, and budget records.

Reference:

- [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
- [m15-operator-verification-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m15-operator-verification-checklist.md)

## Recommended Walkthrough

### 1. Open the dashboard

Route:

- `工作台 -> 数据概览`

Talk track:

- Position the page as the company-level operations view.
- Point out project health, pending confirmations, blocked work, and budget warnings.
- Set up the idea that dashboard numbers come from the same governance records used deeper in the product.

### 2. Move into Project Center

Route:

- `AICoOS -> 项目中心`

Talk track:

- Show one project as the main governed delivery record.
- Highlight current stage, risk, budget health, pending decisions, approvals, and linked tools.
- Open the detail drawer and note that project context, recent activity, and governance summaries are all in one place.

### 3. Show requirement intake and clarification generation

Route:

- `AICoOS -> 需求接收`

Action:

- Open a project intake.
- Run `生成澄清建议`.
- Keep the preview visible before any apply action.

Talk track:

- Explain that vague demand becomes structured intake.
- Show that AI proposes clarification questions, but the operator still decides what becomes a formal project artifact.

### 4. Show Clarification Center

Route:

- `AICoOS -> 澄清中心`

Action:

- Review generated clarification items.
- Optionally run `AI 分析转决策`.

Talk track:

- Clarification is treated as governed work, not just chat output.
- Blockers and unresolved questions remain visible to the operator.

### 5. Show Decision Center and budget suggestion flow

Route:

- `AICoOS -> 决策中心`

Action:

- Open one decision item.
- Run `预算影响建议`.
- Keep the preview visible before applying.

Talk track:

- Once impact becomes meaningful, the platform promotes work into an explicit decision layer.
- Budget and scope implications stay reviewable before any state change.

### 6. Show Approval Center and Budget Center

Routes:

- `AICoOS -> 审批中心`
- `AICoOS -> 预算中心`

Talk track:

- Explain that high-impact changes move through approvals rather than hidden automation.
- Show budget totals, consumption, pending increases, and ledger visibility as governance objects rather than reporting afterthoughts.

### 7. Show workflow and agent-role configuration

Routes:

- `AICoOS -> 工作流模板`
- `AICoOS -> Agent 角色`

Talk track:

- Workflow templates make stage expectations explicit.
- Agent roles define participation and allowed actions, reinforcing governed autonomy rather than free-form agent behavior.

### 8. Show project-side collaboration and external tool controls

Route:

- return to `AICoOS -> 项目中心`

Action:

- In a project drawer, open `会议总结`.
- Optionally open the product-architecture brief preview.
- Open `Linear 映射` and show preview-before-apply.
- Open `Figma 上下文` and show read-only context preview before confirm.

Talk track:

- The runtime helps prepare summaries and briefs.
- External tools are connected, but writes remain operator-confirmed and auditable.

### 9. Close on the boundary

Talk track:

- `agent_runtime` is suggestion-first.
- governance remains in the Spring Boot business system
- the repo is ready for controlled demos and handoff discussion
- the repo is not yet a production-autonomous delivery platform

## Short Version

If time is limited, cover only:

1. Dashboard
2. Project Center
3. Requirement Intake with clarification preview
4. Decision Center with budget suggestion preview
5. One Linear or Figma preview

## Stop Conditions

Pause the demo if any of the following happens:

- the backend or DB is unavailable
- a preview flow writes data before explicit confirmation
- Linear or Figma apply is attempted without first showing preview
- a secret value is exposed in config UI
- dashboard and project-level governance numbers are obviously inconsistent

## Post-Demo Shareout

Send these documents after the walkthrough:

- [aicos-demo-v0.1.md](/Users/hx/it_company/agent_company/docs/release-notes/aicos-demo-v0.1.md)
- [production-gaps.md](/Users/hx/it_company/agent_company/docs/runbooks/production-gaps.md)
- [phase-2-roadmap.md](/Users/hx/it_company/agent_company/docs/plans/phase-2-roadmap.md)
