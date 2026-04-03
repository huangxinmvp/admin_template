# Governance Linkage

M11 introduces a lightweight project governance linkage layer for AICoOS. The backend now maintains a shared `ProjectGovernanceState` snapshot per project and recomputes it from existing clarification, decision, approval, budget, workflow-template, stage, and agent-role records instead of letting each page infer project health independently.

## Core Mechanism

- Snapshot table: `ai_project_governance_state`
- Recompute service: `ProjectGovernanceLinkageService`
- Recompute output includes:
  - current stage and gate status
  - pending and blocker decision counts
  - pending and blocker approval counts
  - clarification and blocker clarification counts
  - budget health
  - failed and blocking gate condition counts
  - missing critical role count
  - overall governance status: `healthy`, `at_risk`, `blocked`
  - governance reason summary
  - last recompute time

## Trigger Points

M11 keeps linkage explicit and service-layer driven. Project governance is recomputed after these write paths:

- project create/update/delete
- requirement intake save
- clarification create/update/delete/mock-generate
- decision create/update/delete and decision actions
- approval create/update/delete and approval actions
- budget plan create/update/delete
- budget ledger create/update/delete
- project stage create/update/delete
- workflow template center create/update
- agent role center create/update

This milestone does not introduce a message bus, workflow engine, or real-time push channel.

## Read Integration

- Project Center list now reads the shared governance snapshot for governance status, stage/gate summary, pending decision counts, and reason summary.
- Project Center detail now shows the shared governance status, reason summary, failed gate counts, missing critical role count, and last recompute time.
- Dashboard aggregation continues to build from project-level detail, but its blocked/risk/bottleneck calculations now align with the shared project governance snapshot.

## Out Of Scope

- automatic workflow execution
- stage auto-transition
- notification or SLA escalation
- approval rule builder
- agent runtime orchestration
- event streaming or analytics pipeline
