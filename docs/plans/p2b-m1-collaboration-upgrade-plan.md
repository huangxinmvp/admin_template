# P2B-M1 Fixed-Scenario Collaboration Upgrade Plan

## Goal

P2B-M1 upgrades the current lightweight collaboration capability so AICoOS feels more like a real AI-driven company in a few high-value fixed scenarios, while still remaining governance-first and suggestion-first.

This milestone improves:

- role framing
- structured output quality
- operator usefulness
- linkage to existing governance modules

It does not introduce autonomous execution, background workers, or a generic orchestration platform.

## Scope

This milestone covers only these three fixed collaboration scenarios:

1. requirement clarification collaboration
2. decision + budget collaboration
3. product + architecture collaboration

This milestone does not cover:

- background agents
- autonomous execution
- auto-approvals
- auto-stage transitions
- auto external writes
- new product-domain modules
- generic multi-agent orchestration

## Current Starting Point

The current M14 collaboration layer already provides:

- fixed entry points in the main UI
- preview-first flows
- operator-confirmed apply/save actions
- existing governed write targets

Current limitation:

- outputs are structurally thin
- role framing is mostly limited to participant names
- governance linkage is implied rather than explicit
- operator guidance is present but not strong enough to feel like a real cross-role company review

## Upgrade Direction

### 1. Requirement Clarification Collaboration

Keep the current apply target:

- selected suggestions still create `ClarificationItem`

Enhance preview output with:

- scenario label
- collaboration summary
- recommended operator action
- governance linkage summary
- role insights per role
- selection guidance

### 2. Decision + Budget Collaboration

Keep the current apply target:

- apply still updates the current `DecisionItem`

Enhance preview output with:

- scenario label
- collaboration summary
- recommended operator action
- governance linkage summary
- product manager view
- budget analyst view
- risk flags

### 3. Product + Architecture Collaboration

Keep the current save target:

- save still writes a `MeetingRecord`

Enhance preview output with:

- scenario label
- collaboration summary
- recommended operator action
- governance linkage summary
- product manager view
- architect view
- architecture focus areas
- delivery implications

## Implementation Areas

### Runtime schemas and payloads

Extend the collaboration response structures in:

- [schemas.py](/Users/hx/it_company/agent_company/agent_runtime/schemas.py)
- [AgentRuntimePayloads.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/runtime/AgentRuntimePayloads.java)

These additions should stay scenario-specific, not generic.

### Mock provider

Upgrade the collaboration outputs in:

- [mock.py](/Users/hx/it_company/agent_company/agent_runtime/providers/mock.py)

Goals:

- stronger role framing
- more actionable operator guidance
- more explicit governance linkage hints
- more stable structured output for local verification

### Backend service mapping

Upgrade:

- [AgentRuntimeSuggestionServiceImpl.java](/Users/hx/it_company/agent_company/web_backend/src/main/java/com/hiking/treasure/modules/phase1/service/impl/AgentRuntimeSuggestionServiceImpl.java)

Changes:

- pass richer current-state context into the runtime requests
- map the new scenario-specific output fields into backend VOs
- preserve existing apply/save boundaries

### Backend VO and frontend types

Extend:

- collaboration VOs under `web_backend/.../domain/vo/`
- collaboration-related frontend types in [types.ts](/Users/hx/it_company/agent_company/web_frontend/src/services/backend/types.ts)

### Frontend collaboration modals

Upgrade preview rendering in:

- [RequirementClarificationCollaborationModal.tsx](/Users/hx/it_company/agent_company/web_frontend/src/pages/aicoos/components/RequirementClarificationCollaborationModal.tsx)
- [DecisionBudgetCollaborationModal.tsx](/Users/hx/it_company/agent_company/web_frontend/src/pages/aicoos/components/DecisionBudgetCollaborationModal.tsx)
- [ProductArchitectureBriefModal.tsx](/Users/hx/it_company/agent_company/web_frontend/src/pages/aicoos/projects/ProductArchitectureBriefModal.tsx)

Target effect:

- make the collaboration output read like a role-based review result
- show governance linkage and “what happens if I apply this”
- improve selection and operator comprehension without changing the core entry structure

### Entry-point copy

Adjust copy only where it helps the operator understand:

- this is a fixed-scenario collaboration tool
- preview comes first
- governed write target stays in the main system

## Validation Plan

- runtime unit tests for collaboration schema changes and mock output
- backend targeted tests or compile/test for payload and VO changes
- frontend `npm run tsc`
- manual verification of all three scenarios:
  - preview renders the upgraded structure
  - apply/save still lands in the same governed targets
  - no autonomous side effects are introduced

## Boundary Reminder

P2B-M1 is an upgrade to fixed-scenario collaboration quality.

It is not:

- a general multi-agent framework
- autonomous orchestration
- background execution
- a new product-domain expansion
