# Dashboard / Operations Cockpit

## Scope

M10 turns the existing governance modules into a company-level control surface.

The dashboard is still the existing `/dashboard/analysis` route, but it now behaves as an AICoOS operations cockpit instead of a generic system-stat page.

## What It Shows

### Overview

Top-level operating metrics:

- total projects
- active projects
- pending decisions
- pending approvals
- blocked projects
- high-risk projects
- budget warning projects
- budget exceeded projects

### Stage Distribution

Projects grouped by current stage so operators can see where delivery work is accumulating.

### Governance Bottlenecks

System-wide bottleneck summaries for:

- blocker clarifications
- pending decisions
- blocking approvals
- budget warning or exceeded projects
- missing critical roles
- failed gate conditions

### Budget Health

Budget status distribution across projects:

- unplanned
- pending
- healthy
- warning
- overrun

### Attention List

A cross-project list of projects that need operator attention, including:

- project
- stage
- risk level
- blocking reason
- budget status
- pending approval count
- pending decision count

### Recent Governance Activity

Recent activity is derived from existing project activity records and timestamps.
There is still no dedicated event-stream infrastructure in M10.

## Backend Structure

The existing `/api/system/dashboard` endpoint remains the dashboard entry point.

`SystemPortalServiceImpl` now enriches the response with AICoOS operations data derived from existing Phase 1 services:

- Project Center detail aggregation
- decision/approval summaries
- budget summaries
- gate-condition summaries
- role-coverage summaries

This keeps the dashboard aligned with project-level governance logic instead of creating a separate analytics model.

## Frontend Structure

The dashboard page stays inside the existing admin shell and reuses standard patterns:

- `PageContainer`
- `ProCard`
- `StatisticCard`
- readable tables and timelines
- direct navigation back into Project Center and governance modules

The visual direction is intentionally operator-focused and B2B-readable rather than chart-heavy.

## Current Limitations

M10 does not include:

- real-time streaming
- predictive analytics
- custom BI pipelines
- agent runtime monitoring
- dedicated event sourcing

The cockpit is currently a lightweight derived read model built from the existing governance data already in the platform.
