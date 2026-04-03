# Rollback Runbook

## Goal

Provide a clear rollback path for the current AICoOS pilot environment when an upgrade or deployment change leaves the environment unsafe for pilot use.

## Rollback Triggers

Rollback is justified when one or more of the following occurs:

- backend startup does not complete
- runtime startup does not complete
- DB/bootstrap changes leave the environment unstable
- login or core shell pages fail
- verified integration paths regress unexpectedly
- browser verification fails on a blocking path
- the issue cannot be corrected safely inside the current maintenance window

## Rollback Procedure

1. stop user-facing activity
2. identify the last known good image tags and env files
3. restore the previous image tags in `compose.env`
4. restore previous env file values if they changed
5. restart the Compose stack on the restored version
6. restore the previous database state only if the failure is caused by schema or data changes and DB restore is approved
7. run post-rollback verification

Canonical command:

```bash
bash scripts/pilot_compose.sh up -d
```

## Config Precautions During Rollback

During rollback, pay special attention to:

- runtime shared key alignment
- backend to runtime base URL
- Linear API key source
- Figma API key source
- system-config versus env override drift

Do not assume the UI shows the actual current secret value:

- password-style values are masked
- backend env overrides may still win over database values

## Post-Rollback Verification

After rollback, verify at minimum:

- [ ] frontend is reachable
- [ ] backend health works
- [ ] runtime health works
- [ ] login works
- [ ] dashboard works
- [ ] one runtime-backed flow works
- [ ] one integration flow works if enabled in pilot scope

Recommended:

- run the browser regression checklist again
- store rollback verification evidence alongside the rollback record

## Rollback Documentation

Record:

- rollback trigger
- affected version
- restored version
- whether DB was restored
- any env file or credential changes also reverted
- final verification outcome

## Boundary Reminder

This rollback runbook is suitable for controlled pilot environments.

It does not provide:

- zero-downtime rollback
- HA failover orchestration
- automated release reversal
- immutable infrastructure recovery patterns
