# Phase 2B Demo Script

## Goal

Run a concise walkthrough of the completed Phase 2B scope.

This demo should show that AICoOS now provides clearer operator guidance and more useful collaboration outputs, while still remaining governance-first and suggestion-first.

## Audience

- product and engineering handoff
- stakeholder review
- internal demo of the completed Phase 2B scope

## Demo Story In One Sentence

"Phase 2B turns AICoOS from a visible governance console into a more guided operating system: collaboration outputs are clearer, governance linkage is stronger, and Project Center now tells operators not just what is wrong, but what to do next."

## Pre-Demo Checks

Before the walkthrough:

1. Start MySQL and the local stack.
2. Start `agent_runtime`.
3. Start Spring Boot.
4. Start the frontend.
5. Confirm admin login works.
6. Confirm there is at least one demo project with:
   - requirement intake
   - clarification items
   - decision items
   - budget data
   - collaboration records or collaboration entry points ready
7. Keep these references open:
   - [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
   - [browser-regression-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/browser-regression-checklist.md)

## Recommended Walkthrough

### 1. Start in Project Center

Route:

- `AICoOS -> 项目中心`

Talk track:

- show the project list as the operator’s main delivery view
- point out that the compact `下一步` hint already tells the operator what likely deserves attention first
- emphasize that this is not automation; it is governance guidance

### 2. Open one project detail drawer

Talk track:

- show that the detail view now combines governance state, collaboration context, and action guidance
- focus on `下一步建议`
- explain the structure:
  - overall judgment
  - hard blockers
  - primary recommended action
  - follow-up actions

Key point:

- the platform is now telling the operator what to do next, why, and where to go

### 3. Show requirement clarification collaboration

Route:

- `AICoOS -> 需求接收`

Action:

- open the collaboration preview for requirement clarification review

Talk track:

- explain that the output is role-based and fixed-scenario, not a generic autonomous swarm
- point out improved role differentiation and clearer recommended operator action
- if appropriate, show that selected items still require explicit apply

### 4. Show decision + budget collaboration

Route:

- `AICoOS -> 决策中心`

Action:

- open one decision and show the collaboration review preview

Talk track:

- highlight that the review now better links decision interpretation to budget implications
- explain that the result is still advisory
- note that no automatic budget change or approval is triggered

### 5. Show product + architecture collaboration

Route:

- return to `AICoOS -> 项目中心`
- open project detail
- open the product-architecture brief entry

Talk track:

- explain that this scenario now reads more like a useful brief than a generic AI summary
- show open questions, risks, and next actions
- reinforce that save still remains explicit and governed

### 6. Return to the action chain

Route:

- `AICoOS -> 项目中心`
- reopen the same project if needed

Talk track:

- connect collaboration outputs back to the action chain
- show that the chain is short, ordered, and operator-facing
- click one target-module action only if you want to prove module continuity

Key point:

- Phase 2B is about interpretation and direction, not hidden execution

### 7. Close on the boundary

Talk track:

- collaboration is stronger, but still fixed-scenario
- guidance is stronger, but still read-only
- the system helps operators move forward, but does not move the project by itself

## Short Version

If time is limited, cover only:

1. Project Center list next-step hint
2. Project Center detail action chain
3. one collaboration preview
4. one jump from the action chain into the target module

## What To Emphasize

During the demo, emphasize these improvements:

- clearer collaboration outputs
- stronger governance linkage
- more useful project-level guidance
- short action chain instead of a single vague recommendation
- continued operator control

## What Not To Oversell

Do not present Phase 2B as:

- autonomous delivery
- general multi-agent orchestration
- automatic workflow execution
- automatic budget control
- automatic stage progression

## Suggested Post-Demo Shareout

After the walkthrough, share:

- [phase-2b-complete.md](/Users/hx/it_company/agent_company/docs/release-notes/phase-2b-complete.md)
- [next-phase-options.md](/Users/hx/it_company/agent_company/docs/plans/next-phase-options.md)
- [production-gaps.md](/Users/hx/it_company/agent_company/docs/runbooks/production-gaps.md)
