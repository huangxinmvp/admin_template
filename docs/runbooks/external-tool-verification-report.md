# External Tool Verification Report

## Purpose

This report records the current external-tool verification state for the local AICoOS environment after the latest Linear and Figma real-environment checks.

This work remained verification-focused:

- no new product features
- no product-scope expansion
- only minimal debugging-oriented integration fixes where needed to clarify real blockers

## Verification Environment

Services used:

- frontend dev server: `http://127.0.0.1:8000`
- Spring Boot backend: `http://127.0.0.1:8081`
- `agent_runtime`: `http://127.0.0.1:8091`

Artifact directory:

- `/tmp/aicoos-m15-check/`

Representative evidence files:

- `/tmp/aicoos-m15-check/linear-postfix-project-apply.json`
- `/tmp/aicoos-m15-check/linear-postfix-decision-comment-apply.json`
- `/tmp/aicoos-m15-check/linear-postfix-audits.json`
- `/tmp/aicoos-m15-check/figma-link-direct-me.json`
- `/tmp/aicoos-m15-check/figma-link-direct-file.json`
- `/tmp/aicoos-m15-check/figma-link-direct-node.json`
- `/tmp/aicoos-m15-check/figma-link-file_key_only-preview.json`
- `/tmp/aicoos-m15-check/figma-link-file_and_node-preview.json`
- `/tmp/aicoos-m15-check/figma-link-file_key_only-apply.json`
- `/tmp/aicoos-m15-check/figma-url-postfix-preview.json`
- `/tmp/aicoos-m15-check/figma-url-postfix-apply.json`
- `/tmp/aicoos-m15-check/figma-link-summary.json`

## Current Integrated Demo Position

Currently verified as runnable:

- internal governance flows
- runtime suggestion flows
- M14 lightweight collaboration flows
- Linear real-environment project mapping and comment export flows
- Figma real-environment preview/read/bind flows through `fileKey` and `fileKey + nodeId`

## Linear Status

### Result

Linear is now **ready for controlled real-environment demo**.

### What was verified live

- project mapping `preview -> apply`
- decision comment `preview -> apply`

Observed live results:

- project mapping created a real Linear issue:
  - identifier: `TEM-5`
  - issue id: `d954de7c-9278-4ca8-a3fb-f1b59766dd25`
  - URL: `https://linear.app/template-hx/issue/TEM-5/aicoos验证-真实-linear-连通性验证修复后`
- decision comment apply succeeded against that real issue
- project bindings and tool audits updated correctly inside AICoOS

## Figma Status

### Result

Figma is now **ready for controlled real-environment demo**.

### What was verified live

Current config/effective runtime state:

- `integration.figma.enabled=true`
- `integration.figma.apiBaseUrl=https://api.figma.com/v1`
- `integration.figma.apiKey` is configured through the existing password-style system-config path

Direct provider checks:

- `GET https://api.figma.com/v1/me` succeeded
- `GET https://api.figma.com/v1/files/YDc6neOeUqoFQRIIHDyt2t` succeeded
- `GET https://api.figma.com/v1/files/YDc6neOeUqoFQRIIHDyt2t/nodes?ids=0:1` succeeded

This confirms:

- the token is valid
- the target file is accessible
- the target node is accessible

Observed AICoOS live results:

- full `figmaUrl` preview now succeeds
- full `figmaUrl` apply now succeeds
- `fileKey=YDc6neOeUqoFQRIIHDyt2t` preview succeeded
- `fileKey=YDc6neOeUqoFQRIIHDyt2t, nodeId=0:1` preview succeeded
- one real apply/bind succeeded and stored a live Figma file binding into AICoOS

Representative preview/apply details:

- full-link preview resolved:
  - file key `YDc6neOeUqoFQRIIHDyt2t`
  - node id `0:1`
  - node name `Page 1`
- file preview resolved file name `AICoOS`
- node preview resolved node name `Page 1`
- apply created a linked binding with external url `https://www.figma.com/file/YDc6neOeUqoFQRIIHDyt2t`

## Screenshot Status

Browser screenshots were still not captured in this pass.

Blocker:

- local Playwright MCP browser startup still fails because it attempts to create `/.playwright-mcp` on a read-only filesystem

Fallback used:

- payload and provider-response evidence were captured under `/tmp/aicoos-m15-check/`

## Recommended Next Step

External-tool readiness is now strong enough for controlled demo usage.

The next cleanup step can stay narrow:

1. optionally capture browser screenshots once the local Playwright filesystem blocker is removed
2. optionally simplify operator guidance now that full-link, `fileKey`, and `fileKey + nodeId` paths are all working

At this point:

- Linear can proceed into controlled real-environment demo usage
- Figma can also proceed into controlled real-environment demo usage
- do not interpret these results as production readiness

## Production Boundary

These results support controlled local and stakeholder demo usage only.

Do not describe this verification state as:

- production-ready
- enterprise-hardened
- deployment-complete

The current conclusion is limited to controlled real-environment demo readiness for the verified Linear and Figma flows.
