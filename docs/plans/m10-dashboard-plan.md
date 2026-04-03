# M10 Dashboard Plan

## Scope

- keep the existing `/api/system/dashboard` endpoint and `/dashboard/analysis` page
- turn them into an AICoOS operations cockpit instead of adding a second dashboard
- derive company-level operational visibility from existing Phase 1 data

## Backend

- extend dashboard response with:
  - overview cards
  - stage distribution
  - governance bottleneck summary
  - budget health summary
  - attention project list
  - recent governance activity
- reuse current Phase 1 services and records:
  - project
  - stage
  - clarification
  - decision
  - approval
  - budget
  - agent role summaries
- keep aggregation lightweight and read-oriented

## Frontend

- update the existing `/dashboard/analysis` page into an operator cockpit
- keep B2B admin readability with:
  - statistic cards
  - summary cards
  - simple stage-distribution visualization
  - attention table/list
  - recent activity timeline/list
- add direct entry points back into Project Center and other governance modules

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`

## Out Of Scope

- real-time streaming
- predictive analytics
- runtime / agent execution monitoring
- custom BI system
- dedicated event-stream infrastructure
