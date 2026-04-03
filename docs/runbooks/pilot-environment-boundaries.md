# Pilot Environment Boundaries

## Goal

State clearly what the current AICoOS pilot version supports and does not support so that operators, stakeholders, and trial users do not mistake pilot readiness for production readiness.

## What The Pilot Supports

The current pilot-ready package supports:

- governed project and stage visibility
- requirement intake
- clarification suggestion preview/apply
- decision suggestion preview/apply
- budget suggestion preview/apply
- approval actions
- dashboard review
- `agent_runtime` suggestion flows
- Linear controlled real-environment preview/apply flows
- Figma controlled real-environment preview/apply flows
- lightweight diagnostics and request correlation
- repeatable single-host Compose deployment
- repeatable browser walkthrough and evidence capture support

## What The Pilot Does Not Support

The current pilot does not support:

- autonomous multi-agent execution
- unrestricted external writes
- bidirectional sync with Linear or Figma
- background reconciliation or polling
- production-grade secret management
- HA deployment guarantees
- disaster recovery maturity
- unattended production operation

## Operator Expectations

The pilot should be treated as:

- controlled
- operator-mediated
- preview-first
- repeatable enough for trial operation

It should not be treated as:

- production-hardened
- self-healing
- autonomous
- multi-host by default

## Supported Deployment Style

The pilot package assumes:

- one backend
- one runtime
- one reachable database
- one frontend or reverse-proxy path
- explicit env file management
- single-host Docker Compose as the reference operating shape

It does not assume:

- autoscaling
- cluster failover
- distributed tracing platform
- dedicated secret manager

## Integration Boundary

Linear and Figma are verified for controlled real-environment demo and pilot use, but:

- they are still explicit operator flows
- they are not background-sync systems
- they are not authoritative sources of truth over AICoOS business state

## Final Boundary

The current system is appropriate for controlled pilot handoff.

It is not yet appropriate to claim:

- production readiness
- enterprise hardening completeness
- autonomous delivery readiness
- vault-backed secret management
- unattended multi-host operation
