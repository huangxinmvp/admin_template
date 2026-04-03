# AICoOS Phase 1 Plan

## Goal

Build Phase 1 of AICoOS by extending the existing Spring Boot and Ant Design admin template into a governed delivery console. Phase 1 should prioritize workflow governance, approvals, budget accountability, and configurable project operations over agent autonomy.

The implementation should stay inside the current `web_backend` and `web_frontend` applications for this phase. A separate `agent-runtime` is a later boundary, not a Phase 1 deliverable.

## Recommended Module Boundaries for Phase 1

### Backend boundary strategy

Keep the existing template modules intact and place all new AICoOS business code under the currently unused `web_backend/src/main/java/com/hiking/treasure/modules/` area. That lets us preserve working template behavior while introducing a module-first organization for new domains.

Recommended backend business modules:

| Module | Core responsibility | Reuse from current template |
| --- | --- | --- |
| Identity & Access Foundation | auth, tenant, user, role, permission, session, audit, menu enforcement | existing auth, tenant, role, user, permission, session, tenant interceptor |
| Project Governance Core | `Project`, `ProjectStage`, `GateCheck`, `ChangeRequest`, requirement intake, stage progression, project dashboard summary | existing CRUD patterns, menus, dashboard shell, tenant model |
| Decisions & Approvals | `DecisionItem`, `ApprovalRecord`, pending confirmations, gate actions, explicit user confirmations | message center, announcements, permission checks, workflow task concepts |
| Budget & Accountability | `BudgetPlan`, `BudgetLedger`, thresholds, action metering hooks, over-budget gate checks | system config, audit metadata, dashboard cards, CRUD scaffolding |
| Workflow Template Center | stage templates, gate rules, approval routing, optional Flowable mapping | existing Flowable APIs and workflow designer surfaces |
| Agent Organization | `AgentRole`, `AgentTask`, assignment suggestions, role capability settings, permission envelopes | role/permission UI patterns, menu system, config pages |
| Artifact & Meeting Record | `Artifact`, `MeetingRecord`, linked files, summaries, traceable outputs | file center, announcement/message patterns, timeline-style UI components |
| Connector Config | external system connection settings and governance flags | existing system config center and admin UX patterns |

### Frontend information architecture

Add business-facing Phase 1 route groups while keeping the current `System Management` and `Account` areas as supporting infrastructure.

Recommended Phase 1 IA:

- `Project Center`
- `Approval Center`
- `Decision Center`
- `Budget Center`
- `Workflow Template Center`
- `Agent Role Management`
- `Basic Project Dashboard`

Keep the following current areas as shared support functions rather than replacing them:

- `System Management`
- `Account`
- selected `Workflow` capabilities where they support templates or approval execution

### External boundary

Phase 1 should not introduce a separate `agent-runtime` service for autonomous execution. Instead:

- Spring Boot remains the business source of truth
- the frontend remains the primary control console
- any future runtime service should be designed as a separate boundary that returns structured outputs to the business backend

If runtime APIs are needed in Phase 1, define interface contracts only. Do not move core governance state out of the current backend.

## Proposed Implementation Sequence

### 1. Stabilize the extension baseline

- Preserve existing auth, menu, session, and tenant behavior.
- Fix or explicitly triage validation drift before heavy feature work, so new AICoOS modules are not added on top of a moving baseline.
- Reserve `modules/` for all new AICoOS backend code and avoid expanding the root technical-layer packages for new business domains.

### 2. Build the Project Governance Core

- Introduce `Project`, `ProjectStage`, `GateCheck`, and `ChangeRequest`.
- Add the Project Center and a basic project dashboard.
- Encode the required AICoOS stage model as explicit enums and transition rules rather than ad hoc workflow variables.

This step creates the main business container that later approvals, budgets, artifacts, and agent tasks will attach to.

### 3. Add Decisions and Approvals

- Introduce `DecisionItem` and `ApprovalRecord`.
- Build pending confirmation queues and approval actions.
- Require explicit confirmation for unclear requirements, budget changes, high-risk actions, and permission escalations.

This is the core governance layer and should arrive before agent-task automation.

### 4. Add Budget and Accountability

- Introduce `BudgetPlan` and `BudgetLedger`.
- Record estimated versus approved versus consumed token budget.
- Add threshold checks that block or escalate when budgets exceed allowed limits.

This step is mandatory before any serious agent-task suggestion or orchestration features.

### 5. Add the Workflow Template Center

- Model reusable stage templates, gate rules, and approval routing.
- Decide where Flowable helps and where explicit AICoOS state machines are clearer.
- Use Flowable as a supporting engine, not as the only representation of the business lifecycle.

The business stage model must remain understandable without requiring BPMN expertise everywhere.

### 6. Add Agent Organization

- Introduce `AgentRole` and `AgentTask`.
- Support role definitions, task suggestions, human approvals, and permission envelopes.
- Keep task creation governed and mockable; do not introduce uncontrolled autonomous execution.

### 7. Add Artifact and Meeting Record

- Introduce `Artifact` and `MeetingRecord`.
- Link uploaded files and generated summaries back to projects, stages, decisions, and approvals.
- Make outputs auditable and reviewable in the console.

### 8. Harden the dashboard and cross-module reporting

- Replace generic admin counts with project, stage, budget, gate, and approval metrics.
- Surface pending confirmations, budget risk, and gate status at the dashboard level.
- Keep the dashboard focused on operational governance, not just system inventory.

## Risks

- If new AICoOS features continue the current layer-first pattern, the business model will sprawl quickly and become hard to govern.
- The tenant interceptor in [MybatisPlusConfig.java](../../web_backend/src/main/java/com/hiking/treasure/config/MybatisPlusConfig.java) only applies to listed tables, so every new tenant-scoped AICoOS table must be added deliberately.
- The current validation drift means future features could land without trustworthy regression signals unless tests and lint are cleaned up.
- The existing Flowable integration is generic. If Phase 1 tries to encode the entire product lifecycle only in BPMN, the governed stage model may become harder to reason about.
- The file center is local-only today, so artifact handling will need a future storage abstraction if the product grows beyond a single-node setup.
- The current dashboard and message center are generic admin surfaces, so they will need domain-specific redesign to communicate project governance health clearly.

## Assumptions

- Phase 1 keeps all core governance logic inside the current Spring Boot backend.
- Phase 1 keeps the Ant Design console as the primary operating interface.
- A separate `agent-runtime` is out of scope for implementation in this phase beyond future contract notes.
- New AICoOS backend code will use module-first boundaries under `modules/` rather than expanding only the shared root technical-layer directories.
- Existing System Management and Account pages remain available as supporting admin infrastructure.
- Documentation for this phase is written in English.

## Validation Status

### Validated in this planning pass

- repo structure and stack choices
- current route and menu strategy
- reusable admin, auth, config, workflow, file, and notification surfaces
- existence of a tenant interceptor and shared CRUD infrastructure
- current command results:
  - `web_backend`: `./mvnw -q test` fails because tests lag the auth and session API
  - `web_frontend`: `npm run lint` fails on `biome.json` plus existing frontend lint issues

### Not validated in this planning pass

- end-to-end startup and manual UI walkthrough
- clean DB initialization and seed flow from scratch
- exact Flowable behavior in a running environment
- performance or scaling behavior for future agent-task workloads

