# AGENTS.md

## Project
AICoOS (AI Company Operating System)

This repository is the main product workspace for an AI-agent-driven IT company platform.
The platform turns software delivery into a governed workflow where AI Agents act as employees,
tokens represent labor budget, and users approve scope, cost, permissions, and release decisions.

## Product Goal
Build a platform that can:
1. accept user project requirements
2. clarify ambiguous requirements
3. estimate feasibility, scope, and token budget
4. create governed project plans
5. assign work to role-based AI Agents
6. integrate with Linear, Figma, code tools, email, and meeting systems
7. enforce a standard software delivery and release process
8. require user confirmation for unclear requirements, budget changes, high-risk actions, and permission escalations

## Current Repo Strategy
This repo is NOT a greenfield rebuild.

We are reusing the existing Spring Boot + Ant Design SaaS admin template as the control console and business foundation.

Use the existing project wherever possible for:
- auth
- tenant/org/user management
- role/permission management
- menus, routing, layout, admin pages
- audit logs
- CRUD-style management modules
- notifications / approval center
- project and workflow configuration UI

Create new services only when the concern clearly does not fit the current template, especially:
- agent runtime
- tool orchestration
- MCP client integrations
- long-running task execution
- model-facing workflow execution
- context routing
- budget metering for agent actions

## High-Level Architecture
This product should evolve into these logical parts:

1. admin console
   - customer workspace
   - project governance center
   - approval center
   - budget center
   - workflow template center
   - agent organization management

2. backend business core
   - project
   - stage / gate
   - decision item
   - approval
   - budget ledger
   - audit trail
   - connector config

3. agent runtime (separate service if needed)
   - orchestration
   - agent task execution
   - context packaging
   - MCP / external tool access
   - meeting synthesis
   - clarification generation

## Core Domain Concepts
When designing code, keep these domain objects explicit:

- Project
- ProjectStage
- GateCheck
- DecisionItem
- ApprovalRecord
- BudgetPlan
- BudgetLedger
- AgentRole
- AgentTask
- ToolCallAudit
- Artifact
- MeetingRecord
- ChangeRequest

Avoid vague generic naming like:
- DataInfo
- CommonEntity
- TempTask
- MiscRecord

Prefer business names that reflect the product language.

## Non-Negotiable Engineering Rules

### 1. Plan before code
Before making significant changes:
- read the existing codebase
- summarize the current structure
- identify reusable modules
- propose a scoped implementation plan
- keep the diff focused

Do not jump directly into large refactors.

### 2. Reuse before rebuild
Prefer extending the current Spring Boot + Ant Design template.
Do not replace the whole project structure unless explicitly instructed.

### 3. Keep scope tight
Only implement the requested milestone.
Do not opportunistically redesign unrelated modules.

### 4. Preserve working behavior
Do not break existing login, layout, permissions, routing, or shared components unless the task explicitly requires it.

### 5. Make incremental changes
For each milestone:
- implement a small, reviewable slice
- run validation
- document what changed
- stop at a stable checkpoint

### 6. Explain structural decisions
If introducing:
- a new service
- a new dependency
- a new top-level module
- a new infrastructure requirement

you must explain:
- why it is needed
- why the existing project cannot reasonably absorb it
- what the migration impact is

### 7. Business governance first
This product is not “just an AI app”.
Priority order:
1. governed workflow
2. budget/accountability
3. approval and permission safety
4. tool integration
5. agent autonomy

If there is tension between autonomy and governance, choose governance.

## UI / Frontend Guidance
Frontend is an Ant Design admin-style console.

Prefer:
- existing layout and routing conventions
- existing table / form / drawer / modal patterns
- clean business dashboards
- configuration-driven pages
- readable admin UX over flashy consumer UI

Do not introduce a brand new UI framework.

For new pages:
- use existing layout shell
- keep information density appropriate for B2B SaaS
- favor tables, cards, timelines, and side panels
- ensure important statuses are visible:
  - current stage
  - pending confirmations
  - budget status
  - risk level
  - gate status

## Backend Guidance
Backend is Spring Boot and should remain the main business system.

Prefer:
- clear module boundaries
- service + domain-oriented naming
- DTO / VO / entity separation if already used in the project
- explicit enums for stage/status/type fields
- auditable write operations
- idempotent workflow transitions where possible

Avoid:
- dumping business logic into controllers
- giant utility classes
- generic untyped maps when a typed DTO/entity is better
- hidden side effects across unrelated modules

## Agent Runtime Guidance
If creating `agent-runtime`:
- keep it separate from the main Spring Boot business backend
- use FastAPI or another lightweight service framework
- start with mockable APIs first
- design for orchestration, not for UI concerns
- expose stable APIs the Java backend can call

First version should support:
- generate clarification questions
- estimate budget
- create agent task suggestions
- produce meeting summaries
- return structured results

Do not implement uncontrolled autonomous execution in v1.

## Workflow Rules
All project execution must follow governed stages.

Minimum stage model:
1. intake
2. clarification
3. feasibility
4. estimation
5. approval
6. planning
7. design
8. development
9. testing
10. release approval
11. release
12. retrospective

No stage should be skipped without an explicit rule.

High-risk actions must be gated:
- production release
- external paid API enablement
- real email sending
- destructive data actions
- permission escalation
- budget increase beyond threshold

## Documentation Rules
For any meaningful feature:
- update related docs under `docs/`
- document new modules and APIs
- keep milestone notes current
- include assumptions and open questions

For release-closeout or demo handoff work:
- write concise release notes under `docs/release-notes/`
- prepare operator-facing runbooks under `docs/runbooks/`
- capture known limitations and production gaps explicitly
- write the recommended next-phase roadmap under `docs/plans/`

When asked to plan, write output into:
- `docs/plans/`
- `docs/architecture/`
- `docs/modules/`

## Validation Rules
After each milestone:
- run backend build/tests if available
- run frontend build/lint if available
- verify routes/pages load if possible
- verify no obvious type/import/build errors
- summarize what was validated and what was not

If something cannot be validated, explicitly say so.

## Git / Diff Rules
Keep diffs small and reviewable.
Do not mix:
- business feature work
- formatting-only edits
- unrelated refactors

Prefer one milestone = one coherent commit/PR.

## First-Phase Product Focus
Phase 1 is about the platform console and governance foundation.

Build first:
- project center
- requirement intake
- decision item / pending confirmation center
- budget center
- workflow template center
- agent role management
- approval center
- basic project dashboard

Do NOT prioritize first:
- full autonomous multi-agent collaboration
- production deployment automation
- complex Figma generation
- full Linear bi-directional sync
- unrestricted code execution

## Expected Working Style
When given a task:
1. inspect current repo
2. locate reusable modules
3. propose a short implementation plan
4. implement the smallest useful slice
5. validate
6. summarize changed files, results, and open issues

If requirements are ambiguous, ask or create a structured assumptions list before coding.
