# Governance Action Chain

## Purpose

P2B-M5 upgrades Project Center next-step guidance from a single recommendation into a short governance action chain.

The action chain helps operators understand:

- 现在最该先做什么
- 后面可以顺着做什么
- 每一步为什么值得做
- 它主要在消解什么阻塞或风险
- 做完之后项目治理状态大概率会发生什么变化

## Scope

This module is read-only and suggestion-first.

It does not:

- auto-execute actions
- auto-create approvals
- auto-update budgets
- auto-advance stages
- introduce a workflow engine

## Backend Structure

The chain is still derived inside the existing next-step resolver:

- [ProjectNextStepGuidanceResolver.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/service/impl/ProjectNextStepGuidanceResolver.java)

The existing guidance payload is extended through:

- [ProjectNextStepGuidanceVO.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/domain/vo/ProjectNextStepGuidanceVO.java)
- [ProjectNextStepActionVO.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/domain/vo/ProjectNextStepActionVO.java)

Each action node now carries:

- `actionOrder`
- `actionLabel`
- `targetModule`
- `recommendedReason`
- `addressedRisk`
- `expectedChange`

## Derivation Model

The action chain keeps the existing blocker-first priority order:

1. blocker clarifications
2. blocker decisions
3. blocker approvals
4. failed or blocking gate conditions
5. budget warning / pending / overrun / unplanned
6. missing critical roles
7. collaboration-driven follow-up
8. healthy stage-progress fallback

The resolver produces:

- one primary action
- up to two follow-up actions

The first action is the operational anchor for the detail drawer CTA and the compact list-page hint.

## Project Center UX

Project Center now treats the chain as:

- `recommendedNextStep`: short summary headline
- `priorityActions[0]`: primary recommended action
- `priorityActions[1..]`: follow-up actions

The detail drawer surfaces:

- headline recommendation
- priority / actor / target module
- current blockers
- primary action card
- follow-up actions list

The project list remains compact and continues to show only the top-level next-step signal.

## Operator Boundary

Even when the chain points to modules such as:

- `clarification-center`
- `decision-center`
- `approval-center`
- `budget-center`
- `agent-roles`
- `workflow-templates`

the system still requires the operator to open that module and explicitly decide what to do next.

This module improves governance guidance quality only. It does not replace human confirmation.
