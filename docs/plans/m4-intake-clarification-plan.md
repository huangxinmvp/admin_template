# M4 Requirement Intake And Clarification Plan

## Scope

Build the first customer-facing intake and clarification workflow inside the existing Phase 1 module so a project can carry structured requirement intake data, project-scoped clarification items, and visible requirement-governance signals in Project Center.

## Backend

- add `RequirementIntake` as a lightweight project-linked intake record
- add `ClarificationItem` as a project-linked clarification question record
- keep both inside `modules/phase1` with the same entity + DTO + VO + controller + service patterns used in M2
- extend project-center aggregation to derive:
  - requirement completeness
  - clarification count
  - blocker clarification count
  - intake summary signals for the project detail drawer
- support temporary mock clarification generation with a small helper endpoint that inserts canned items when a project has sparse intake data

## Frontend

- add a dedicated intake page for create/edit on top of an existing project
- add a dedicated clarification center page with:
  - project selector / project context
  - clarification list
  - create/edit modal
  - optional mock-generate action
- extend Project Center detail to show:
  - requirement completeness
  - clarification count
  - blocker count
  - entry points to intake and clarification pages

## Limits

- no real agent runtime or model generation
- no workflow automation or full stage transitions
- no email, calendar, or connector automation
- attachments remain placeholders only in M4

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`
- document the resulting module structure in:
  - `docs/modules/requirement-intake.md`
  - `docs/modules/clarification-center.md`
