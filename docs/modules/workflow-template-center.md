# Workflow Template Center

## Scope

M8 introduces the explicit governance template layer for AICoOS. A workflow template now acts as the configurable operating rule set for a project type instead of leaving stage order, gate checks, and approval expectations implicit in scattered records.

This milestone stays configuration-oriented:

- no BPMN designer
- no visual workflow engine
- no automatic stage transitions
- no notification or SLA escalation

## Backend Structure

Backend code stays inside the existing Phase 1 module tree:

- `WorkflowTemplate`
  - template header and governance rule text fields
  - project type, version, enabled/default status
- `WorkflowTemplateStage`
  - child rows for stage order, enabled flag, description, and note
- `WorkflowTemplateService`
  - center-specific page/detail/create/update methods
- `WorkflowTemplateCenterController`
  - dedicated center APIs under `/api/aicoos/workflowTemplate/center`

Supporting bootstrap/config updates:

- `bootstrap-aicoos-phase1-domain.sql`
  - extends `ai_workflow_template`
  - creates `ai_workflow_template_stage`
- `TemplateSystemBootstrapConfig`
  - backfills new template columns for existing databases
- `MybatisPlusConfig`
  - adds tenant handling for `ai_workflow_template_stage`

## Frontend Structure

Frontend replaces the generic workflow-template CRUD route with a dedicated center page:

- `/aicoos/workflow-templates`
  - list page
  - create/edit modal
  - detail drawer

The page reuses the existing admin template patterns:

- `PageContainer`
- `ProTable`
- `ModalForm`
- right-side detail `Drawer`

## Configuration Model

### Template Header

Each template stores:

- template code
- template name
- project type
- version number
- status
- default flag
- description
- remark

### Stage Configuration

Each template can configure a lightweight stage sequence with:

- stage code
- stage name
- stage order
- enabled flag
- stage description
- stage note

Default creation starts from the standard AICoOS lifecycle:

1. intake
2. clarification
3. feasibility
4. estimation
5. approval
6. planning
7. design
8. development
9. testing
10. release approval
11. release
12. retrospective

### Governance Rule Sections

Rule sections are intentionally explicit text configs for M8:

- gate checks
- blocking decision conditions
- blocking approval conditions
- budget threshold conditions
- high-risk action approval requirements

This keeps the model auditable and editable without introducing a rule DSL too early.

## Project Center Integration

Project Center now consumes workflow-template context in a lightweight way:

- shows workflow template name
- shows current template stage
- shows current gate-condition summaries
- counts blocking gate conditions in governance summary

Current gate-condition status is derived from existing project data:

- stage gate status
- pending/blocking decisions
- pending/blocking approvals
- budget health
- project risk level

## Current Limitations

M8 does not yet provide:

- rule execution engine
- automatic stage progression
- cross-template version migration
- template inheritance
- automated approval triggering
- connector-driven workflow actions

Those concerns should build on top of this configuration layer rather than replacing it.
