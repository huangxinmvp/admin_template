# Collaboration Governance Linkage

## Purpose
- P2B-M2 turns the existing fixed-scenario collaboration outputs into clearer governance signals inside the current AICoOS modules.
- The milestone does not add new collaboration scenarios or autonomous execution.
- Spring Boot remains the source of truth and all writes still happen only after explicit operator apply/save actions.

## Scope

### 1. Requirement Clarification Collaboration
- Collaboration preview now carries stronger governance context derived from current project state:
  - project governance status
  - existing/open/blocker clarification counts
  - pending decision / approval counts
  - escalation-suggestion count
  - suggested follow-up modules
- Apply behavior is unchanged:
  - selected suggestions still create `ClarificationItem`
  - collaboration origin and governance hints remain stored in `remark`

### 2. Decision + Budget Collaboration
- Collaboration preview now shows clearer linkage to the current governance situation:
  - decision status / blocker flag
  - project governance status
  - current budget health
  - pending / blocker approval signals
  - whether the review only recommends budget confirmation versus actually creating approvals
- Apply behavior is unchanged:
  - the current `DecisionItem` is updated
  - no approval is auto-created
  - no budget recalculation runs automatically

### 3. Product + Architecture Collaboration
- Collaboration preview now explains how the brief fits current project governance:
  - governance status
  - blocker / risk flags
  - pending decision / approval counts
  - failed gate / missing critical role counts
  - explicit save-path meaning
- Save behavior is unchanged:
  - the result is still stored as `MeetingRecord`
  - no new heavyweight artifact module is introduced

## Project Center
- Project Center adds a dedicated `协作治理联动` section derived entirely from existing records:
  - collaboration total
  - collaboration-created clarification counts
  - escalation-to-decision suggestion counts
  - decision-budget review counts
  - budget-confirmation recommendation counts
  - product-architecture brief counts
- It also surfaces scenario-level operational summaries:
  - requirement collaboration follow-up modules and blocker/open counts
  - decision-budget collaboration latest reviewed decision and pending/blocker state
  - product-architecture latest brief title, summary, open question count, action-item count, and decision-candidate count

## Data Derivation
- No new tables are introduced.
- Signals are derived from existing persisted markers:
  - `ClarificationItem.remark` containing `来源：多角色澄清评审`
  - `ClarificationItem.remark` containing `建议人工判断后提升至决策中心`
  - `ClarificationItem.remark` containing `建议跟进模块：...`
  - `DecisionItem.remark` containing `已应用多角色协作评审建议`
  - `DecisionItem.remark` containing `建议补充预算确认`
  - `MeetingRecord.sourceType = product_architecture_brief`

## Boundaries
- No autonomous execution
- No automatic approval creation
- No automatic budget recalculation
- No automatic stage transitions
- No new general multi-agent orchestration layer
