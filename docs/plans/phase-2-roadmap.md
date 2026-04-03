# AICoOS Phase 2 Roadmap

## Phase 2 Goal

Phase 2 should make the current demo foundation more reliable, enforceable, and handoff-ready before pursuing broader autonomy.

The priority is not "more AI features first." The priority is turning the current governance surfaces into a more operationally credible system.

## Recommended Workstreams

### 1. Baseline stabilization and packaging

Priority:

- clean up the release baseline into a reviewable branch or commit
- reduce validation drift in backend tests and frontend lint or build checks
- make demo data and local verification more repeatable
- tighten the handoff path so demos do not depend on tribal knowledge

Why first:

- the current product story is strong, but the repo still carries packaging and validation debt
- Phase 2 should start from a cleaner operational checkpoint than the current demo handoff baseline

### 2. Governance execution hardening

Priority:

- strengthen stage transition rules
- make gate checks more enforceable
- connect approvals, decisions, and budget states more tightly
- improve release-readiness and blocked-state consistency

Why next:

- the product already surfaces governance state
- the next step is making that governance more executable and less interpretive

### 3. Budget and accountability deepening

Priority:

- improve budget recalculation consistency
- enforce threshold-driven review and escalation rules
- sharpen role-based budget attribution
- make ledger and decision history easier to audit during project reviews

Why next:

- budget already exists as a first-class object
- it now needs clearer downstream consequences and better operator trust

### 4. Runtime trust and traceability

Priority:

- improve real-provider verification
- capture clearer prompt, input, and result traceability for operator review
- strengthen preview/apply consistency across suggestion flows
- improve failure messaging and fallback behavior without removing operator control

Why next:

- the current value of `agent_runtime` depends on confidence and reviewability
- trustworthiness is more important than expanding autonomy

### 5. Integration maturity

Priority:

- deepen Linear mapping and export usability
- improve Figma context quality and operator guidance
- strengthen integration verification in real environments
- expand audit visibility before considering any broader external automation

Why next:

- external tools are already connected in controlled form
- maturity should come from safer verification and better operator ergonomics, not from silent automation

### 6. Deployment and production-readiness track

Priority:

- introduce better secret-management strategy
- document environment promotion and rollback expectations
- define runtime supervision and deployment ownership
- add observability, alerting, and operational health checks appropriate for the stack

Why next:

- the current demo can be run locally
- production handoff requires much stronger operational discipline than the demo baseline provides

## Early Phase 2 Non-Priorities

Do not prioritize first:

- uncontrolled autonomous multi-agent execution
- background writes to external tools without operator confirmation
- full bidirectional sync with Linear or Figma
- unrestricted code execution
- "AI magic" UI work that hides governance state

## Suggested Sequence

1. Baseline stabilization and packaging
2. Governance execution hardening
3. Budget and accountability deepening
4. Runtime trust and traceability
5. Integration maturity
6. Deployment and production-readiness

## Target Exit Criteria

Phase 2 should aim to leave the repo in a state where:

- the demo baseline is packaged as a cleaner reviewable release snapshot
- governance state drives more consistent behavior, not just reporting
- budget and approval controls have clearer operational consequences
- runtime suggestions are easier to trust and audit
- Linear and Figma flows are easier to verify in real environments
- deployment preparation is materially closer to a controlled pilot
