# Upgrade Runbook

## Goal

Provide a safe, repeatable upgrade sequence for the current AICoOS pilot environment using the Compose-first baseline.

This runbook is intended for controlled pilot environments, not zero-downtime enterprise operations.

## Pre-Upgrade Checks

Before starting:

- [ ] current pilot environment is stable
- [ ] current `compose.env`, `backend.env`, and `agent_runtime.env` are backed up
- [ ] current deployed image tags are recorded
- [ ] database backup plan is confirmed
- [ ] browser regression baseline from the current version is available

## Backup Expectations

Minimum backup expectations before upgrade:

- [ ] database backup exists
- [ ] current env files are recoverable
- [ ] current image tags are recoverable
- [ ] current frontend/backend/runtime behavior has a quick verification baseline

## Upgrade Procedure

1. stop or drain user-facing activity as appropriate for the pilot
2. back up the database
3. record current image tags and env files
4. update `compose.env`, `backend.env`, or `agent_runtime.env` only if the release requires it
5. pull or rebuild the new images
6. restart the Compose stack
7. run health checks
8. run targeted browser verification

Canonical commands:

```bash
bash scripts/pilot_compose.sh pull
bash scripts/pilot_compose.sh up -d
bash scripts/pilot_stack_preflight.sh
```

If the upgrade depends on fresh local builds instead of pre-published images:

```bash
bash scripts/pilot_compose.sh up -d --build
```

## Migration And Bootstrap Considerations

Current repository behavior relies on backend startup for:

- Flowable schema bootstrap
- AICoOS bootstrap SQL
- system-config seed alignment

Upgrade cautions:

- verify database reachability before backend start
- verify backend health returns `databaseReady=true`
- stop the upgrade if backend startup logs show bootstrap or schema failure

## Post-Upgrade Verification

Minimum checks:

- [ ] frontend is reachable
- [ ] backend health works
- [ ] runtime health works
- [ ] login works
- [ ] dashboard works
- [ ] one runtime-backed flow works
- [ ] one Linear flow works if enabled in pilot scope
- [ ] one Figma flow works if enabled in pilot scope

## Known Upgrade Risks

- image tag drift
- env file drift
- DB/bootstrap mismatch
- outdated integration credentials
- frontend proxy misrouting
- runtime provider not ready after restart

## When To Stop And Roll Back

Rollback should be considered if any of the following holds after the upgrade:

- backend cannot start cleanly
- runtime cannot start cleanly
- login is broken
- verified integration paths regress unexpectedly
- preflight or browser verification shows a blocking failure that cannot be corrected quickly and safely

If rollback is needed, use:

- [rollback-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/rollback-runbook.md)
