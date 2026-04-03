#!/usr/bin/env bash

set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://127.0.0.1:8081}"
RUNTIME_URL="${RUNTIME_URL:-http://127.0.0.1:8091}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"

failures=0

check_cmd() {
  local cmd="$1"
  if command -v "$cmd" >/dev/null 2>&1; then
    printf '[ok] command: %s\n' "$cmd"
  else
    printf '[missing] command: %s\n' "$cmd"
    failures=$((failures + 1))
  fi
}

check_port() {
  local label="$1"
  local host="$2"
  local port="$3"
  if bash -lc ">/dev/tcp/${host}/${port}" >/dev/null 2>&1; then
    printf '[ok] %s port reachable: %s:%s\n' "$label" "$host" "$port"
  else
    printf '[warn] %s port unreachable: %s:%s\n' "$label" "$host" "$port"
    failures=$((failures + 1))
  fi
}

check_http() {
  local label="$1"
  local url="$2"
  if curl -fsS "$url" >/dev/null 2>&1; then
    printf '[ok] %s HTTP reachable: %s\n' "$label" "$url"
  else
    printf '[warn] %s HTTP unreachable: %s\n' "$label" "$url"
    failures=$((failures + 1))
  fi
}

printf '== Local command checks ==\n'
check_cmd java
check_cmd node
check_cmd npm
check_cmd python3
check_cmd curl

printf '\n== Local service checks ==\n'
check_port "MySQL" "$DB_HOST" "$DB_PORT"
check_http "Agent Runtime health" "${RUNTIME_URL}/health"
check_http "Spring Boot health" "${BACKEND_URL}/api/system/health"

printf '\n== Summary ==\n'
if [ "$failures" -eq 0 ]; then
  echo "Preflight passed."
  exit 0
fi

echo "Preflight found ${failures} issue(s)."
echo "Tips:"
echo "- Start MySQL with: docker compose -f docker-compose.local.yml up -d mysql"
echo "- Start runtime with: bash scripts/start_agent_runtime_mock.sh"
echo "- Start backend with: bash scripts/start_backend_local.sh"
echo "- Frontend should point to ${BACKEND_URL}"
exit 1
