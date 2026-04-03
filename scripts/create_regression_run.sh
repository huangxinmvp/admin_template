#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="${1:-/tmp/aicoos-regression}"
LABEL="${2:-smoke}"
ENVIRONMENT="${3:-local}"
TIMESTAMP="$(date '+%Y%m%d-%H%M%S')"
RUN_ID="${TIMESTAMP}-${LABEL}"
RUN_DIR="${ROOT_DIR%/}/${RUN_ID}"

mkdir -p "${RUN_DIR}/screenshots" "${RUN_DIR}/notes" "${RUN_DIR}/api"

cat > "${RUN_DIR}/notes/run-summary.md" <<EOF
# Regression Run Summary

- Run ID: \`${RUN_ID}\`
- Environment: \`${ENVIRONMENT}\`
- Created At: \`$(date '+%Y-%m-%d %H:%M:%S %Z')\`
- Verifier:
- Stack Endpoints:
  - Frontend:
  - Backend:
  - Runtime:
- Overall Result:
- Notes:
EOF

cat > "${RUN_DIR}/notes/blockers.md" <<EOF
# Blockers

- None recorded yet.
EOF

printf '%s\n' "${RUN_DIR}"
