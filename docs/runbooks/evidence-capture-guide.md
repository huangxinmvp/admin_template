# Evidence Capture Guide

## Goal

Provide a practical, repeatable evidence-capture path for local and pilot verification runs.

This guide prioritizes reliability over perfect automation.

## Current Recommended Artifact Root

Use:

- `/tmp/aicoos-regression/<run-id>/`

Recommended `run-id` format:

- `YYYYMMDD-HHMMSS-short-label`

Example:

- `/tmp/aicoos-regression/20260331-203000-pilot-smoke/`

## Recommended Artifact Structure

Create these subdirectories for each run:

- `screenshots/`
- `notes/`
- `api/`

Example:

- `/tmp/aicoos-regression/20260331-203000-pilot-smoke/screenshots/`
- `/tmp/aicoos-regression/20260331-203000-pilot-smoke/notes/`
- `/tmp/aicoos-regression/20260331-203000-pilot-smoke/api/`

Current helper script:

```bash
scripts/create_regression_run.sh /tmp/aicoos-regression pilot-smoke local
```

## What To Store

### screenshots

Store:

- page states before and after key actions
- preview dialogs before apply
- final success or failure states for integration actions

Suggested naming:

- `01-login-or-dashboard.png`
- `02-dashboard.png`
- `03-project-center.png`
- `04-clarification-preview.png`
- `05-decision-preview.png`
- `06-budget-preview.png`
- `07-approval-action.png`
- `08-dashboard-follow-through.png`
- `09-linear-preview-or-result.png`
- `10-figma-preview-or-result.png`
- `11-m14-collaboration.png`

### notes

Store:

- a run summary markdown file
- short observations for each flow
- clear pass/fail/blocker notes

Suggested files:

- `run-summary.md`
- `blockers.md`

### api

Store:

- payload snapshots
- key response bodies
- integration verification JSON

Use this for:

- debugging failed browser steps
- preserving evidence when screenshots alone are insufficient

## Current Tooling Reality

### Browser automation status

Current Playwright MCP browser use is blocked on this workstation because it tries to create:

- `/.playwright-mcp`

That path is on a read-only filesystem in the current environment.

### Practical workaround

Use:

- manual browser walkthrough
- filesystem-based screenshot capture
- saved API evidence under the run directory

This is acceptable for P2A-M3 because the goal is repeatable verification and evidence capture, not full browser automation at any cost.

### Helper scripts

The current lightweight support path is:

- [create_regression_run.sh](/Users/hx/it_company/agent_company/scripts/create_regression_run.sh)
- [collect_regression_evidence.sh](/Users/hx/it_company/agent_company/scripts/collect_regression_evidence.sh)
- [capture_regression_screenshot.sh](/Users/hx/it_company/agent_company/scripts/capture_regression_screenshot.sh)

Use them together:

```bash
RUN_DIR=$(scripts/create_regression_run.sh /tmp/aicoos-regression pilot-smoke local)
scripts/collect_regression_evidence.sh "$RUN_DIR"
scripts/capture_regression_screenshot.sh "$RUN_DIR" 01-login-or-dashboard fullscreen 0
```

## Current Screenshot Capture Path

On this workstation, the practical screenshot path is:

- macOS native `screencapture`

Availability check already confirmed:

- `/usr/sbin/screencapture`

Example usage:

### Interactive capture

```bash
/usr/sbin/screencapture -i /tmp/aicoos-regression/<run-id>/screenshots/04-clarification-preview.png
```

### Delayed full-screen capture

```bash
/usr/sbin/screencapture -x -T 3 /tmp/aicoos-regression/<run-id>/screenshots/02-dashboard.png
```

Use interactive mode for modal- or drawer-focused evidence, and delayed full-screen capture when you need to switch focus before the shot.

If screenshot capture fails, the helper script writes:

- `screenshots/<name>.capture-failed.txt`

This keeps the evidence trail intact even when the workstation cannot produce a real image.

## API Evidence Capture Path

When a browser step needs supporting evidence:

- save JSON responses under `/tmp/aicoos-regression/<run-id>/api/`

Typical examples:

- health responses
- preview payload results
- integration audit snapshots
- binding snapshots

## Minimal Repeatable Procedure

1. create the run directory and its three subdirectories
2. collect initial stack evidence
3. run the browser walkthrough from [browser-regression-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/browser-regression-checklist.md)
4. take screenshots at the named checkpoints
5. save any supporting API payloads into `api/`
6. write a short `run-summary.md`

## Suggested `run-summary.md` Contents

Include:

- run id
- date/time
- verifier
- environment type: local, demo, or pilot
- stack endpoints used
- overall pass/fail
- blocker list
- links to notable screenshots and API evidence files

## What This Guide Does Not Provide

This guide does not provide:

- full browser automation
- screenshot annotation tooling
- centralized evidence dashboards
- long-term artifact retention rules

Those are valid future improvements, but they are outside the P2A-M3 scope.
