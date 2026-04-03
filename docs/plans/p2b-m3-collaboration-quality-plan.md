# P2B-M3 Collaboration Quality Plan

## Scope
- Keep the same fixed collaboration scenarios:
  - requirement clarification review
  - decision + budget review
  - product + architecture brief
- Improve output quality and operator usability only.
- Do not add autonomous execution, automatic approvals, budget changes, stage transitions, or a general orchestration layer.

## Implementation
- Runtime:
  - tighten mock collaboration outputs so they are shorter, less repetitive, and more role-distinct
  - update the OpenAI-compatible prompts to prefer concise, differentiated, operator-ready outputs
- Backend:
  - add lightweight normalization for collaboration responses to dedupe repeated points, suppress low-value filler, and keep lists short
  - make recommended operator actions more concrete before they reach the UI
- Frontend:
  - increase signal density in the collaboration preview modals
  - surface concise “what to do now” and role-specific takeaways first, while de-emphasizing repeated narrative text

## Validation
- Run `python3 -m unittest agent_runtime.tests.test_app`
- Run backend `./mvnw -q -DskipTests compile`
- Run frontend `npm run tsc`
