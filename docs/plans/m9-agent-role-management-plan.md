# M9 Agent Role Management Plan

## Scope

- keep `AgentRole` as the root organizational role record
- extend it with explicit business fields:
  - role type
  - description
  - responsibility summary
  - default flag
  - budget factor
  - approval-collaboration flag
- add lightweight child config for:
  - stage participation
  - allowed actions

## Backend

- extend `AgentRole` with the new governance/org fields
- add `AgentRoleStageParticipation` for per-stage involvement:
  - stage code / name
  - participation type
  - note
- add `AgentRoleAllowedAction` for explicit action permissions:
  - action code / name
  - allowed flag
  - approval required flag
  - note
- add dedicated center endpoints and center service methods
- extend Project Center aggregation with:
  - linked roles
  - current-stage recommended roles
  - missing critical roles when current stage lacks required enabled roles

## Frontend

- replace the generic `/aicoos/agent-roles` page with a dedicated Agent Role Management Center
- list page shows:
  - role name
  - role type
  - enabled/default state
  - budget factor
  - approval collaboration
  - update time
- detail/edit surface includes:
  - role base info
  - stage participation config
  - allowed action config
- Project Center detail shows:
  - linked role summary
  - current-stage recommended roles
  - missing critical roles

## Validation

- backend: `./mvnw -q -DskipTests compile`
- frontend: `npm run tsc`

## Out Of Scope

- agent runtime orchestration
- model routing / fallback
- autonomous collaboration
- complex permission DSL
- per-project runtime task assignment
