#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${PILOT_COMPOSE_FILE:-${ROOT_DIR}/deploy/pilot/compose.yaml}"
COMPOSE_ENV_FILE="${PILOT_COMPOSE_ENV_FILE:-${ROOT_DIR}/deploy/pilot/compose.env}"
RUN_ROOT="${1:-/tmp/aicoos-pilot-diagnostics}"
TIMESTAMP="$(date '+%Y%m%d-%H%M%S')"
BUNDLE_DIR="${RUN_ROOT%/}/${TIMESTAMP}"
API_DIR="${BUNDLE_DIR}/api"
LOG_DIR="${BUNDLE_DIR}/logs"
NOTES_DIR="${BUNDLE_DIR}/notes"
LOG_TAIL="${PILOT_DIAGNOSTICS_LOG_TAIL:-200}"

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

resolve_env_path() {
  local base_dir="$1"
  local value="$2"
  if [ -z "$value" ]; then
    printf '%s\n' ""
    return 0
  fi
  case "$value" in
    /*)
      printf '%s\n' "$value"
      ;;
    *)
      printf '%s\n' "${base_dir}/${value#./}"
      ;;
  esac
}

probe_host() {
  local host="$1"
  case "$host" in
    "" | 0.0.0.0 | ::)
      printf '127.0.0.1'
      ;;
    *)
      printf '%s' "$host"
      ;;
  esac
}

write_note() {
  local name="$1"
  shift
  {
    for line in "$@"; do
      printf '%s\n' "$line"
    done
  } > "${NOTES_DIR}/${name}"
}

capture_http() {
  local url="$1"
  local file="$2"
  shift 2
  if curl -fsS "$@" "$url" > "${file}"; then
    return 0
  fi
  {
    echo "request_failed=true"
    echo "url=${url}"
  } > "${file}"
  return 1
}

extract_access_token() {
  local file="$1"
  sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p' "${file}"
}

sanitize_auth_response() {
  sed -E \
    -e 's/"accessToken":"[^"]*"/"accessToken":"<redacted>"/g' \
    -e 's/"refreshToken":"[^"]*"/"refreshToken":"<redacted>"/g'
}

mkdir -p "${API_DIR}" "${LOG_DIR}" "${NOTES_DIR}"

COMPOSE_ENV_DIR="$(cd "$(dirname "${COMPOSE_ENV_FILE}")" && pwd)"
load_env_file "${COMPOSE_ENV_FILE}"
BACKEND_ENV_FILE="$(resolve_env_path "${COMPOSE_ENV_DIR}" "${BACKEND_ENV_FILE:-./backend.env}")"
RUNTIME_ENV_FILE="$(resolve_env_path "${COMPOSE_ENV_DIR}" "${AGENT_RUNTIME_ENV_FILE:-./agent_runtime.env}")"
load_env_file "${BACKEND_ENV_FILE}"
load_env_file "${RUNTIME_ENV_FILE}"

BACKEND_URL="${BACKEND_URL:-http://$(probe_host "${PILOT_BACKEND_BIND_HOST:-127.0.0.1}"):${PILOT_BACKEND_PORT:-8081}}"
RUNTIME_URL="${RUNTIME_URL:-http://$(probe_host "${PILOT_RUNTIME_BIND_HOST:-127.0.0.1}"):${PILOT_RUNTIME_PORT:-8091}}"
FRONTEND_URL="${FRONTEND_URL:-http://$(probe_host "${PILOT_FRONTEND_BIND_HOST:-127.0.0.1}"):${PILOT_FRONTEND_PORT:-8080}}"
BACKEND_USERNAME="${PILOT_DIAGNOSTICS_USERNAME:-admin}"
BACKEND_PASSWORD="${PILOT_DIAGNOSTICS_PASSWORD:-Admin@123456}"

docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" ps -a > "${NOTES_DIR}/compose-ps.txt"

for service in mysql agent_runtime backend frontend; do
  docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" logs --tail "${LOG_TAIL}" "${service}" \
    > "${LOG_DIR}/${service}.log" 2>&1 || true
done

capture_http "${BACKEND_URL}/api/system/health" "${API_DIR}/backend-health.json" || true
capture_http "${RUNTIME_URL}/health" "${API_DIR}/runtime-health.json" || true
capture_http "${FRONTEND_URL}/" "${API_DIR}/frontend-index.html" || true

AUTH_LOGIN_RAW="${API_DIR}/auth-login.raw.json"
if capture_http "${BACKEND_URL}/api/auth/login" "${AUTH_LOGIN_RAW}" \
  -X POST -H "Content-Type: application/json" \
  -d "{\"username\":\"${BACKEND_USERNAME}\",\"password\":\"${BACKEND_PASSWORD}\"}"; then
  BACKEND_TOKEN="$(extract_access_token "${AUTH_LOGIN_RAW}")"
  sanitize_auth_response < "${AUTH_LOGIN_RAW}" > "${API_DIR}/auth-login.json"
  rm -f "${AUTH_LOGIN_RAW}"
  if [ -n "${BACKEND_TOKEN}" ]; then
    capture_http "${BACKEND_URL}/api/system/diagnostics" "${API_DIR}/backend-diagnostics.json" \
      -H "Authorization: Bearer ${BACKEND_TOKEN}" || true
  else
    write_note "backend-diagnostics-note.txt" \
      "backend_login_succeeded_but_token_missing=true" \
      "check=${API_DIR}/auth-login.json"
  fi
else
  if [ -f "${AUTH_LOGIN_RAW}" ]; then
    sanitize_auth_response < "${AUTH_LOGIN_RAW}" > "${API_DIR}/auth-login.json"
    rm -f "${AUTH_LOGIN_RAW}"
  fi
  write_note "backend-diagnostics-note.txt" \
    "backend_login_failed=true" \
    "check=${API_DIR}/auth-login.json"
fi

if [ -n "${AGENT_RUNTIME_API_KEY:-}" ]; then
  capture_http "${RUNTIME_URL}/health/diagnostics" "${API_DIR}/runtime-diagnostics.json" \
    -H "X-Agent-Runtime-Key: ${AGENT_RUNTIME_API_KEY}" || true
else
  write_note "runtime-diagnostics-note.txt" \
    "runtime_api_key_missing=true" \
    "check=${RUNTIME_ENV_FILE}"
fi

cat > "${NOTES_DIR}/summary.md" <<EOF
# Pilot Diagnostics Bundle

- Created At: \`$(date '+%Y-%m-%d %H:%M:%S %Z')\`
- Bundle Dir: \`${BUNDLE_DIR}\`
- Frontend URL: \`${FRONTEND_URL}\`
- Backend URL: \`${BACKEND_URL}\`
- Runtime URL: \`${RUNTIME_URL}\`
- Compose Env: \`${COMPOSE_ENV_FILE}\`
- Backend Env: \`${BACKEND_ENV_FILE}\`
- Runtime Env: \`${RUNTIME_ENV_FILE}\`
- Log Tail: \`${LOG_TAIL}\`

Stored artifacts:

- \`notes/compose-ps.txt\`
- \`logs/*.log\`
- \`api/backend-health.json\`
- \`api/runtime-health.json\`
- \`api/frontend-index.html\`
- optional \`api/auth-login.json\`
- optional \`api/backend-diagnostics.json\`
- optional \`api/runtime-diagnostics.json\`
EOF

printf '%s\n' "${BUNDLE_DIR}"
