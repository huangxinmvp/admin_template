#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PILOT_COMPOSE_ENV_FILE="${PILOT_COMPOSE_ENV_FILE:-${ROOT_DIR}/deploy/pilot/compose.env}"

failures=0

load_env_file() {
  local env_file="$1"
  if [ ! -f "$env_file" ]; then
    return 0
  fi
  set -a
  # shellcheck disable=SC1090
  . "$env_file"
  set +a
}

probe_host() {
  local host="$1"
  case "$host" in
    "" | "0.0.0.0" | "::")
      printf '127.0.0.1'
      ;;
    *)
      printf '%s' "$host"
      ;;
  esac
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
    printf '[ok] %s reachable: %s\n' "$label" "$url"
  else
    printf '[warn] %s failed: %s\n' "$label" "$url"
    failures=$((failures + 1))
  fi
}

check_backend_health() {
  local body
  body="$(curl -fsS "${BACKEND_URL}/api/system/health" 2>/dev/null || true)"
  if [ -n "$body" ] && printf '%s' "$body" | grep -q '"status":"ok"' && printf '%s' "$body" | grep -q '"databaseReady":true'; then
    printf '[ok] backend health reachable: %s/api/system/health\n' "$BACKEND_URL"
  else
    printf '[warn] backend health failed: %s/api/system/health\n' "$BACKEND_URL"
    failures=$((failures + 1))
  fi
}

check_runtime_health() {
  local body
  body="$(curl -fsS "${RUNTIME_URL}/health" 2>/dev/null || true)"
  if [ -n "$body" ] && printf '%s' "$body" | grep -q '"status":"ok"' && printf '%s' "$body" | grep -q '"defaultProviderReady":true'; then
    printf '[ok] runtime health reachable: %s/health\n' "$RUNTIME_URL"
  else
    printf '[warn] runtime health failed: %s/health\n' "$RUNTIME_URL"
    failures=$((failures + 1))
  fi
}

load_env_file "${PILOT_COMPOSE_ENV_FILE}"

DB_HOST="${DB_HOST:-$(probe_host "${PILOT_DB_BIND_HOST:-127.0.0.1}")}"
DB_PORT="${DB_PORT:-${PILOT_DB_PORT:-3306}}"
BACKEND_URL="${BACKEND_URL:-http://$(probe_host "${PILOT_BACKEND_BIND_HOST:-127.0.0.1}"):${PILOT_BACKEND_PORT:-8081}}"
RUNTIME_URL="${RUNTIME_URL:-http://$(probe_host "${PILOT_RUNTIME_BIND_HOST:-127.0.0.1}"):${PILOT_RUNTIME_PORT:-8091}}"
FRONTEND_URL="${FRONTEND_URL:-http://$(probe_host "${PILOT_FRONTEND_BIND_HOST:-127.0.0.1}"):${PILOT_FRONTEND_PORT:-8080}}"

printf '== Pilot stack checks ==\n'
check_port "MySQL" "$DB_HOST" "$DB_PORT"
check_runtime_health
check_backend_health
check_http "Frontend" "${FRONTEND_URL}/"

printf '\n== Summary ==\n'
if [ "$failures" -eq 0 ]; then
  echo "Pilot preflight passed."
  exit 0
fi

echo "Pilot preflight found ${failures} issue(s)."
echo "Expected order: database -> agent_runtime -> backend -> frontend or reverse proxy"
exit 1
