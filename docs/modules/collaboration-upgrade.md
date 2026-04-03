# Collaboration Upgrade

## Purpose

P2B-M1 upgrades the current M14 lightweight collaboration layer so the fixed collaboration scenarios feel more like real business-role reviews inside an AI-driven company, while preserving the governance-first operating model.

This module does not introduce:

- autonomous execution
- background workers
- generic multi-agent orchestration
- auto-approvals
- auto-stage transitions
- auto external writes

## What P2B-M1 Changes Compared With M14

M14 established:

- fixed-scenario collaboration
- preview-first operator interaction
- governed apply/save behavior

P2B-M1 strengthens that layer by adding:

- stronger role framing
- richer structured output
- clearer operator guidance
- more explicit linkage back to existing governance modules

## Upgraded Collaboration Scenarios

### 1. Requirement Clarification Collaboration

Roles:

- Requirement Analyst
- Product Manager

Preview now emphasizes:

- scenario label
- collaboration summary
- recommended operator action
- governance linkage to the clarification module
- role-specific insights
- selection guidance for which suggestions to apply first

Apply target remains:

- selected items still create `ClarificationItem`

What it still does not do:

- no autonomous clarification creation during preview
- no auto-promotion to decision
- no auto-stage change

### 2. Decision + Budget Collaboration

Roles:

- Product Manager
- Budget Analyst

Preview now emphasizes:

- scenario label
- collaboration summary
- recommended operator action
- governance linkage to the current `DecisionItem`
- role-specific views from product and budget perspectives
- risk flags for operator judgment

Apply target remains:

- apply still updates the current `DecisionItem`

What it still does not do:

- no automatic approval creation
- no automatic budget recalculation
- no automatic decision status transition

### 3. Product + Architecture Collaboration

Roles:

- Product Manager
- Architect

Preview now emphasizes:

- scenario label
- collaboration summary
- recommended operator action
- governance linkage back to the project context
- role-specific product and architecture views
- architecture focus areas
- delivery implications

Save target remains:

- save still writes a `MeetingRecord`

What it still does not do:

- no new product domain module
- no design workflow automation
- no approval or stage automation

## Governance Boundary

The main system remains the source of truth.

That means:

- preview is always informational first
- operators still choose when to apply or save
- governed writes still land in existing modules only
- the collaboration layer remains an enhancement to decision quality, not an execution engine

## Current Write Targets

P2B-M1 still uses the same governed targets:

- requirement clarification collaboration -> `ClarificationItem`
- decision + budget collaboration -> `DecisionItem`
- product + architecture collaboration -> `MeetingRecord`

## Design Boundary

P2B-M1 is deliberately fixed-scenario only.

It should be described as:

- a role-framed collaboration upgrade
- a suggestion-first business review layer
- a governed operator tool

It should not be described as:

- a general multi-agent platform
- autonomous orchestration
- self-directed collaboration loops
