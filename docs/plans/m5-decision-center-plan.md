# M5 Decision Center Plan

## Scope

Build a formal Decision Center on top of the existing `DecisionItem` skeleton so important requirement and governance uncertainties can be reviewed, confirmed, rejected, deferred, and tracked as explicit operator-facing records.

## Backend

- extend `DecisionItem` with lightweight confirmation fields:
  - `sourceType`
  - `sourceId`
  - `description`
  - `impactSummary`
  - `suggestedOptions`
  - `recommendedOption`
  - `budgetImpactSummary`
  - `projectImpactSummary`
  - `blockerFlag`
- add a small `DecisionActionLog` table to persist confirm / reject / defer actions with comment, operator, and status transition
- add custom decision endpoints for:
  - action history
  - confirm / reject / defer action
  - manual promotion from clarification item
- keep promotion explicit and lightweight:
  - manual promotion from a clarification item creates a decision prefilled from clarification data
  - clarification record stores the promoted decision link
- extend Project Center aggregation to include blocker decision count while keeping recent decision rendering in place

## Frontend

- replace the generic decision route with a dedicated Decision Center page
- build:
  - decision list table
  - detail drawer
  - action modal for confirm / reject / defer
  - history timeline/list
- add manual promotion entry points from Clarification Center
- add Project Center links into Decision Center for project-scoped review

## Limits

- no email, calendar, or notification automation
- no full approval workflow
- no budget auto-recalculation
- no automatic project-stage transitions

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`
- document the module in `docs/modules/decision-center.md`
