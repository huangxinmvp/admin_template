# AICoOS Phase 2A Pilot Readiness

## Goal

Phase 2A turns the current AICoOS Demo v0.1 foundation into a pilot-ready version that is easier to deploy, configure, verify, and operate in a controlled real environment.

Phase 2A prioritizes operational readiness and controlled trial usability before any broader autonomy or product-surface expansion.

## Phase Framing

Phase 2A is not a feature-growth phase first.

The priority order for this phase is:

1. environment and config governance
2. secret and config hygiene
3. browser-level E2E and regression support
4. observability and diagnostics
5. pilot-facing product hardening

This phase should improve repeatability, operator confidence, and trial readiness without changing the governance-first product boundary.

## Phase 2A Milestones

### P2A-M1: Environment and Config Governance

Goal:

- establish a clean, reproducible environment and configuration foundation for backend, frontend, runtime, and external integrations

Deliverables:

- [p2a-m1-environment-config-plan.md](/Users/hx/it_company/agent_company/docs/plans/p2a-m1-environment-config-plan.md)
- [environment-config-guide.md](/Users/hx/it_company/agent_company/docs/runbooks/environment-config-guide.md)

Focus:

- startup order clarity
- environment separation rules
- config precedence rules
- integration config ownership
- local/demo/pilot environment notes

### P2A-M2: Secret and Config Hygiene

Target direction:

- reduce ambiguity around which values belong in environment variables versus system-config
- tighten handling rules for pilot secrets
- improve operator understanding of masked and overridden values

### P2A-M3: Browser-Level E2E and Regression Support

Target direction:

- make browser verification less dependent on ad hoc manual steps
- improve repeatable UI smoke coverage for core governance and integration flows

### P2A-M4: Observability and Diagnostics

Target direction:

- improve diagnostic clarity for backend, runtime, and external integration failures
- make request tracing and operator troubleshooting faster during pilot operation

### P2A-M5: Pilot-Facing Product Hardening

Target direction:

- harden the current verified demo flows for controlled trial use
- reduce operator friction and trial-time confusion without expanding product scope

## Explicit Non-Goals For Phase 2A

Do not treat Phase 2A as the place to prioritize:

- new autonomous product capabilities
- full multi-agent execution
- bidirectional sync expansion
- workflow redesign unrelated to pilot readiness
- Phase 2B capability work

## Current Starting Point

The repo already has a strong demo foundation:

- internal governance flows are runnable
- runtime suggestion flows are runnable
- M14 lightweight collaboration flows are runnable
- Linear is verified for controlled real-environment demo use
- Figma is verified for controlled real-environment demo use

The remaining gap is not basic product viability. The remaining gap is operational discipline and trial repeatability.

## Phase 2A Success Criteria

Phase 2A should leave the repo in a state where:

- local, demo, and pilot environments are described clearly and reproducibly
- config ownership and precedence are explicit for operators and developers
- secrets and non-secrets are handled with clearer rules
- core flows are easier to verify end to end in a browser
- failures are easier to diagnose during a pilot
- the product is easier to hand off into a controlled pilot environment without implying production maturity

## Production Boundary

Phase 2A is about pilot readiness, not production signoff.

Even after this phase, the platform should still avoid claiming:

- full production readiness
- enterprise hardening completeness
- HA or disaster-recovery maturity by default
- unrestricted autonomous operation
