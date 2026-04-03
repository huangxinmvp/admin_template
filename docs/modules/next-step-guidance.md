# Next-Step Guidance

## Purpose
`Next-Step Guidance` turns existing governance signals into an operator-facing recommendation for what should happen next at the project level.

It is advisory only:
- no automatic stage transition
- no automatic approval action
- no automatic budget update
- no automatic workflow execution

## Inputs
The guidance resolver reads from the existing Project Center view model inputs:
- governance summary
- requirement summary
- decision summary
- approval summary
- budget summary
- collaboration summary
- current gate conditions
- missing critical roles
- current-stage recommended roles
- current stage context

## Priority Order
The resolver applies a deterministic priority order:
1. blocker clarifications
2. blocker decisions
3. blocker approvals
4. failed or blocking gates
5. budget overrun / pending / warning / unplanned
6. missing critical roles
7. collaboration-driven follow-up
8. healthy stage-progress fallback

This means the Project Center always shows one dominant recommendation, while still exposing other hard blockers in `currentBlockers`.

## Output Shape
Each guidance result includes:
- `recommendedNextStep`
- `recommendedReason`
- `currentBlockers`
- `recommendedActorRole`
- `recommendedTargetModule`
- `recommendedPriority`
- `priorityActions`

## Target Module Mapping
- `requirement-intake` -> Requirement Intake
- `clarification-center` -> Clarification Center
- `decision-center` -> Decision Center
- `approval-center` -> Approval Center
- `budget-center` -> Budget Center
- `agent-roles` -> Agent Role Management
- `workflow-templates` -> Workflow Template Center
- `project-center` -> Project Center

## Actor Fallback Rules
When current-stage recommended roles clearly match the needed action, the resolver prefers those role names.

If not, it falls back to business-role labels:
- `需求分析 / 产品经理`
- `产品经理`
- `审批人 / PMO`
- `预算分析 / 产品经理`
- `PMO / 资源协调`
- `交付负责人 / PMO`
- `项目负责人 / 平台操作人`

## UI Placement
- Project list: compact `下一步` hint with priority and actor
- Project detail: top-level `下一步建议` card before the summary stats
- When the target module is `project-center`, the detail card shows an on-page handling hint instead of a self-jump button

This keeps Project Center focused on operator direction rather than raw status alone.
