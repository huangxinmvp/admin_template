# P2B-M4 Next-Step UX Verification

## Goal

Validate that Project Center's read-only `下一步` guidance is easy to scan, easy to understand, and correctly linked to the existing governance modules.

This runbook is for manual operator UX verification only. It does not introduce any new product behavior.

## Scope

Verify these five areas:

- list-page `下一步` readability
- detail-drawer `下一步建议` clarity
- blocker / reason / action consistency
- target-module jump correctness
- fallback role-label quality

## Browser Checks

### 1. Project list readability

- Open `AICoOS -> 项目中心`
- Review at least 3 projects with different governance states if possible:
  - blocked
  - at-risk
  - healthy
- Confirm:
  - the recommendation text is the first thing your eye lands on
  - priority tag and actor stay visually secondary
  - long recommendation text truncates cleanly
  - long actor text does not crowd the recommendation

### 2. Detail-drawer clarity

- Open one blocked project
- Open one healthy or at-risk project
- In each drawer, confirm the `下一步建议` card can be understood quickly:
  1. recommendation
  2. meta row
  3. reason
  4. blockers
  5. priority actions
- Confirm the card still feels like guidance, not a hidden write workflow

### 3. Consistency checks

- For a blocked project, compare the card with:
  - governance summary
  - blocker counts
  - gate conditions
  - budget status when relevant
- Confirm:
  - `当前阻塞` matches the project-level governance signals
  - `recommendedReason` explains the same dominant issue
  - `优先动作` points at the module that actually contains the next operator task

### 4. Jump correctness

- Click the target-module action for available guidance types
- Confirm the jump lands on the expected existing module:
  - `需求接收`
  - `澄清中心`
  - `决策中心`
  - `审批中心`
  - `预算中心`
  - `Agent 角色`
  - `工作流模板`
- Confirm project context is preserved where applicable
- Confirm a `project-center` recommendation shows a passive hint instead of a self-jump button

### 5. Fallback role-label quality

- Find at least one project where the role is a generic fallback instead of a specific configured role
- Confirm the label reads naturally for a Chinese-speaking operator, for example:
  - `需求分析 / 产品经理`
  - `审批人 / PMO`
  - `预算分析 / 产品经理`
  - `交付负责人 / PMO`
- Record any label that still feels too internal, too English-heavy, or too vague

## Expected Outcomes

A good result should feel like this:

- list page: “我一眼就知道这个项目下一步该做什么”
- detail page: “我知道为什么建议这样做、谁应该处理、该去哪个模块”
- jump action: “我点进去就是下一步真正要处理的地方”

## Common Ambiguities To Watch

- recommendation is clear, but the reason sounds more generic than the blockers
- blockers mention one domain, while the action button points to another module
- fallback role labels sound like internal implementation terms instead of operator-facing roles
- `project-center` recommendations look like clickable actions even though they should stay on-page
- the list page actor label visually overpowers the recommendation text

## Pass / Fail Recording

Record one short note for each checked project:

- project name
- governance state
- overall clarity: pass / needs polish / fail
- jump correctness: pass / fail / not applicable
- copy ambiguity: short note if any

Suggested summary format:

```md
- Project: AICoOS Demo Alpha
- Governance state: blocked
- List readability: pass
- Detail clarity: needs polish
- Jump correctness: pass
- Notes: fallback role label is understandable; reason text could be slightly more concrete
```
