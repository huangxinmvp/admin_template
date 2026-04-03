# M8 Workflow Template Center Plan

## Scope

- keep `WorkflowTemplate` as the template root record
- add lightweight `WorkflowTemplateStage` records for ordered stage configuration
- store governance rule sections as explicit text configuration fields on `WorkflowTemplate`
- add dedicated center endpoints and a dedicated admin page instead of relying on generic CRUD only

## Backend

- extend `WorkflowTemplate` with:
  - `projectType`
  - `gateChecksConfig`
  - `blockingDecisionConfig`
  - `blockingApprovalConfig`
  - `budgetThresholdConfig`
  - `highRiskApprovalConfig`
- add `WorkflowTemplateStage` table/entity with:
  - template link
  - stage code / name
  - stage order
  - enabled flag
  - description
  - note
- add `/api/aicoos/workflowTemplate/center/page`
- add `/api/aicoos/workflowTemplate/center/{id}`
- add create/update save endpoints for the center payload
- extend project center aggregation with:
  - workflow template name
  - lightweight current gate condition summaries

## Frontend

- replace the generic `/aicoos/workflow-templates` page with a dedicated Workflow Template Center
- list page shows:
  - template name
  - project type
  - version
  - enabled/default state
  - stage count
  - updated time
- drawer/editor surface includes:
  - base template fields
  - ordered stage configuration list
  - governance rule text sections
- project center detail shows template context and current blocking gate conditions

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`

## Out Of Scope

- BPMN designer
- visual workflow engine
- automatic stage execution
- automatic gate enforcement
- notification or SLA escalation
