# P2B-M5 Governance Action Chain Plan

## Summary

Upgrade the existing read-only `nextStepGuidance` layer into a short governance action chain that recommends 1-3 ordered operator actions.

The chain remains suggestion-first:

- no automatic execution
- no automatic approval creation
- no automatic budget mutation
- no automatic stage transition

## Implementation Scope

### Backend

- extend `ProjectNextStepActionVO` from a title-only item into a richer read-side action node
- keep derivation inside the existing `ProjectNextStepGuidanceResolver`
- continue using the current blocker-first priority order
- generate:
  - one primary action aligned with the current top recommendation
  - up to two follow-up actions
  - per-action explanation fields:
    - action label
    - target module
    - why recommended
    - addressed blocker/risk
    - expected follow-up change
- keep list-page hint compact by reusing the first action as the summary signal

### Frontend

- reuse the existing Project Center list and detail drawer
- keep the current `下一步` list column compact
- upgrade the detail drawer `下一步建议` card into a clearer action-chain presentation:
  - primary action first
  - optional follow-up actions after it
  - blocker/risk and expected result visible per action
  - existing target-module jump remains explicit and manual

### Validation

- extend resolver-focused backend tests for action-chain ordering and action metadata
- keep `ProjectServiceImplTest` coverage aligned with the existing Project Center payload checks
- run focused backend tests and frontend TypeScript validation

## Assumptions

- no new domain tables or persisted governance-chain state
- no new collaboration scenarios
- no new workflow engine behavior
- action-chain data is derived on read from existing governance summary, collaboration summary, stage context, and current module mappings
