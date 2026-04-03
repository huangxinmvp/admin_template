# Collaboration Quality Upgrade

## Purpose
- P2B-M3 improves the quality and operator usefulness of the existing fixed-scenario collaboration outputs.
- The goal is not to expand scope, but to make collaboration previews feel more like concise role-based team outputs and less like repetitive multi-agent summaries.

## What Changed

### 1. Runtime output quality
- The mock collaboration provider now produces:
  - shorter collaboration summaries
  - more concrete recommended operator actions
  - clearer role differentiation
  - fewer repetitive follow-up points
  - stronger scenario-specific governance hints
- The OpenAI-compatible prompts now explicitly ask for:
  - concise one-sentence summaries
  - differentiated role viewpoints
  - concrete operator actions
  - suppression of repeated points across summary, role views, hints, and next steps

### 2. Backend normalization
- Spring Boot now performs a lightweight quality pass on collaboration results before returning them to the UI:
  - dedupe repeated list items
  - limit low-value list length
  - sort clarification suggestions by blocker/escalation importance
  - normalize and trim verbose text
  - backfill more concrete fallback operator actions where needed
- This means both mock output and future real-provider output get the same quality guardrail.

### 3. Preview signal density
- Collaboration preview modals now place higher-signal items first:
  - concise collaboration conclusion
  - concrete recommended operator action
  - role-specific takeaways
  - governance context
- Repetitive narrative text is reduced, and suggestion/result sections are more action-oriented.

## Scenario Notes

### Requirement Clarification Review
- Better distinguishes:
  - input-completion work owned by the Requirement Analyst perspective
  - commitment-impact questions emphasized by the Product Manager perspective
- Suggestion cards highlight:
  - governance reason
  - suggested follow-up module
  - whether human escalation to decision handling is advised

### Decision + Budget Review
- Better distinguishes:
  - business decision framing from the Product Manager
  - budget exposure and confirmation pressure from the Budget Analyst
- Preview makes it clearer what should happen now:
  - update the current decision
  - optionally consider manual budget confirmation
  - optionally consider manual approval

### Product + Architecture Brief
- Better distinguishes:
  - Product Manager focus on first-phase value and boundary
  - Architect focus on constraints, system edges, and implementation order
- Preview now reads more like an operator-facing solution brief with clearer save-and-follow-up guidance.

## Boundaries
- No new collaboration scenarios
- No autonomous execution
- No automatic approvals
- No automatic budget updates
- No automatic stage transitions
- No general orchestration framework
