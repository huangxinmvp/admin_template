# P2A-M3 Browser Regression and Evidence Capture Plan

## Goal

P2A-M3 makes the current AICoOS pilot-ready version easier to verify repeatedly through browser-level walkthroughs and stable evidence capture.

This milestone is about repeatable operator verification, not new product features and not a full browser automation platform rollout.

## Why This Milestone Now

After P2A-M2:

- environment/config guidance is documented
- credential handling and rotation rules are documented
- core demo flows are verified
- Linear and Figma are both verified for controlled real-environment demo use

The next bottleneck is repeatability:

- can an operator rerun the key browser flows consistently?
- can a verifier leave behind a usable evidence pack?
- can failures be reviewed later without depending on memory alone?

P2A-M3 should solve those problems with the lightest workable process.

## Scope

P2A-M3 covers:

- a repeatable browser-level walkthrough for the key flows
- a practical screenshot and evidence-capture path
- a standard artifact directory structure for regression runs
- documentation for repeatable operator verification

P2A-M3 does not cover:

- a new end-to-end product feature
- a full browser automation platform
- observability stack work
- Phase 2B work

## Key Flows In Scope

The browser regression baseline should cover:

- login
- project center
- requirement intake
- clarification suggestion preview/apply
- decision suggestion preview/apply
- budget suggestion preview/apply
- approval actions
- dashboard review
- Linear preview/apply
- Figma preview/apply
- M14 collaboration entry points

## Current Reality To Design Around

### Browser automation limit

The current Playwright MCP browser path is not reliable on this workstation because it attempts to create:

- `/.playwright-mcp`

That path is currently read-only, which blocks the tool before the walkthrough even starts.

### Practical local workaround

The current workstation does have:

- a working browser-accessible frontend
- backend and runtime local health checks
- macOS `screencapture` available at `/usr/sbin/screencapture`

This is enough to support a repeatable manual walkthrough with filesystem-based evidence capture.

### Focused support added in this milestone

The repo now includes lightweight helper scripts for repeatable regression runs:

- [create_regression_run.sh](/Users/hx/it_company/agent_company/scripts/create_regression_run.sh)
- [collect_regression_evidence.sh](/Users/hx/it_company/agent_company/scripts/collect_regression_evidence.sh)
- [capture_regression_screenshot.sh](/Users/hx/it_company/agent_company/scripts/capture_regression_screenshot.sh)

## Planned Deliverables

### 1. Milestone plan

[p2a-m3-browser-regression-plan.md](/Users/hx/it_company/agent_company/docs/plans/p2a-m3-browser-regression-plan.md)

Purpose:

- define the scope and boundaries of P2A-M3

### 2. Browser regression checklist

[browser-regression-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/browser-regression-checklist.md)

Purpose:

- provide one repeatable browser walkthrough for the pilot-critical flows
- define pass/fail points and stop conditions

### 3. Evidence capture guide

[evidence-capture-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/evidence-capture-guide.md)

Purpose:

- define artifact storage layout
- define screenshot, notes, and API evidence conventions
- provide a practical fallback when browser automation is blocked

## Acceptance Criteria

P2A-M3 is complete when:

- one browser-level regression walkthrough exists for the current pilot-critical flows
- the walkthrough is ordered and repeatable
- the artifact directory structure is explicit
- there is a practical evidence capture path that works even when Playwright MCP is blocked
- the docs make clear that this is repeatable verification support, not full test automation

## Validation Plan

Validation for this milestone is process validation:

- confirm the key flow list matches the currently verified product surfaces
- confirm the artifact directory structure can be created locally
- confirm the fallback screenshot command exists on this workstation
- confirm helper scripts can create a run directory and collect stack evidence
- confirm screenshot failure is preserved as evidence rather than disappearing silently
- confirm the docs align with the current local stack endpoints and existing runbooks

## Expected Follow-On Gaps Before P2A-M4

After P2A-M3, the main remaining gaps before P2A-M4 will be:

- no stronger diagnostics aggregation yet
- no richer request-trace workflow for operators yet
- no centralized regression dashboard yet
- browser regression remains primarily manual rather than automated
- screenshot capture still depends on workstation-native tooling rather than a unified in-app recorder
