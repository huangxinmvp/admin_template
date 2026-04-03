#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "usage: $0 <run-dir>" >&2
  exit 1
fi

RUN_DIR="$1"
API_DIR="${RUN_DIR}/api"
NOTES_DIR="${RUN_DIR}/notes"

BACKEND_URL="${BACKEND_URL:-http://127.0.0.1:8081}"
RUNTIME_URL="${RUNTIME_URL:-http://127.0.0.1:8091}"
FRONTEND_URL="${FRONTEND_URL:-http://127.0.0.1:8000}"

mkdir -p "${API_DIR}" "${NOTES_DIR}"

capture_http() {
  local label="$1"
  local url="$2"
  local file="$3"
  if curl -fsS "${url}" > "${file}"; then
    printf '[ok] %s -> %s\n' "${label}" "${file}"
  else
    {
      echo "request_failed=true"
      echo "url=${url}"
    } > "${file}"
    printf '[warn] %s failed -> %s\n' "${label}" "${file}" >&2
  fi
}

capture_http "backend-health" "${BACKEND_URL}/api/system/health" "${API_DIR}/backend-health.json"
capture_http "runtime-health" "${RUNTIME_URL}/health" "${API_DIR}/runtime-health.json"

{
  echo "# Stack Info"
  echo
  echo "- Captured At: \`$(date '+%Y-%m-%d %H:%M:%S %Z')\`"
  echo "- Frontend URL: \`${FRONTEND_URL}\`"
  echo "- Backend URL: \`${BACKEND_URL}\`"
  echo "- Runtime URL: \`${RUNTIME_URL}\`"
  echo "- Git Branch: \`$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)\`"
  echo "- Git Commit: \`$(git rev-parse HEAD 2>/dev/null || echo unknown)\`"
  echo "- Node Version: \`$(node --version 2>/dev/null || echo unavailable)\`"
  echo "- Python Version: \`$(python3 --version 2>/dev/null || echo unavailable)\`"
  echo "- Java Version:"
  java -version 2>&1 | sed 's/^/  /'
} > "${NOTES_DIR}/stack-info.md"

printf '%s\n' "${RUN_DIR}"
