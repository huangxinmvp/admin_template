# AICoOS Demo v0.1 Release Notes

## Release Position

AICoOS Demo v0.1 is the current controlled demo package for the repository.

This version packages the Phase 1 governance foundation on top of the existing Spring Boot and Ant Design admin template. It is intended for guided demos, stakeholder walkthroughs, implementation handoff, and phase-planning. It is not a production release.

## What This Version Packages

### Core console surfaces

- Dashboard and operations cockpit
- Project Center
- Requirement Intake
- Clarification Center
- Decision Center
- Approval Center
- Budget Center
- Workflow Template Center
- Agent Role Management

### Supporting delivery flows

- project detail governance summary and recent activity
- meeting summary generation and meeting record saving
- lightweight collaboration previews for clarification, decision-budget review, and product-architecture brief
- system config visibility for runtime and tool integration settings

### Controlled AI and tool integration flows

- suggestion-first `agent_runtime` APIs for clarification, decision, budget, and meeting support
- Linear preview/apply flows for project, clarification, and decision export
- Figma preview/apply flows for project context linkage
- tool-call audit visibility in project context

### Demo and handoff support

- local startup helpers
- local stack preflight checks
- release-closeout docs, runbooks, and phase planning notes

## Demo Narrative This Release Supports

This version is best presented as a governed software delivery console where:

1. projects move through explicit stages and review points
2. AI produces structured suggestions instead of taking hidden actions
3. budget, approval, and risk remain visible to operators
4. external tools are integrated through preview-first, auditable actions

## Important Boundaries

This release does not claim:

- autonomous multi-agent execution
- unattended external writes
- bidirectional sync with Linear or Figma
- production-grade secret management
- production deployment automation or HA operations

## Recommended Handoff Bundle

Use this release note together with:

- [demo-script.md](/Users/hx/it_company/agent_company/docs/runbooks/demo-script.md)
- [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
- [production-gaps.md](/Users/hx/it_company/agent_company/docs/runbooks/production-gaps.md)
- [phase-2-roadmap.md](/Users/hx/it_company/agent_company/docs/plans/phase-2-roadmap.md)

## Validation Snapshot

The release-closeout work in this pass is documentation packaging only.

Operational validation for the demo version still depends on:

- a reachable local MySQL environment
- a running backend
- a running frontend
- a running mock or configured `agent_runtime`
- operator verification through the existing runbooks

The authoritative current limitations and production-readiness gaps are captured in [production-gaps.md](/Users/hx/it_company/agent_company/docs/runbooks/production-gaps.md).
