# M15 Production Hardening Plan

## Scope
- Harden the current platform for local runnability, repeatable demo verification, and safer integration behavior without adding new product features.

## Implementation
- Add a lightweight local startup/preflight toolchain:
  - optional local MySQL `docker-compose.local.yml`
  - preflight and demo helper scripts under `scripts/`
- Add minimal request correlation and outgoing header propagation:
  - frontend request header
  - Spring Boot request filter
  - runtime / Linear / Figma outgoing `X-Request-Id`
- Tighten external/runtime call handling:
  - timeout defaults remain explicit
  - one safe retry on transient/network failures
  - clearer operator-facing error classification
- Improve config hygiene:
  - support environment override for system-config values
  - keep masked UI behavior
  - document that DB-backed config is still not a production-grade secret store

## Documentation
- Add/update runbooks for:
  - local demo startup order
  - browser walkthrough and operator verification
  - Spring Boot -> runtime flow
  - Linear / Figma real-environment checks
  - deployment preparation and known production gaps

## Validation
- Run runtime tests, backend compile and targeted integration/runtime tests, and frontend `npm run tsc`.
- Validate helper scripts with non-destructive local checks where possible.
