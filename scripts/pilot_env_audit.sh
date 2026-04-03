#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_ENV_FILE="${PILOT_COMPOSE_ENV_FILE:-${ROOT_DIR}/deploy/pilot/compose.env}"
COMPOSE_ENV_DIR="$(cd "$(dirname "${COMPOSE_ENV_FILE}")" && pwd)"

failures=0
warnings=0

fail() {
  printf '[error] %s\n' "$1" >&2
  failures=$((failures + 1))
}

warn() {
  printf '[warn] %s\n' "$1" >&2
  warnings=$((warnings + 1))
}

info() {
  printf '[info] %s\n' "$1"
}

load_env_file() {
  local env_file="$1"
  if [ ! -f "$env_file" ]; then
    fail "env file not found: $env_file"
    return 1
  fi
  set -a
  # shellcheck disable=SC1090
  . "$env_file"
  set +a
}

resolve_env_path() {
  local value="$1"
  if [ -z "$value" ]; then
    printf '%s\n' ""
    return 0
  fi
  case "$value" in
    /*)
      printf '%s\n' "$value"
      ;;
    *)
      printf '%s\n' "${COMPOSE_ENV_DIR}/${value#./}"
      ;;
  esac
}

require_env() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    fail "required env is missing: ${name}"
  fi
}

reject_placeholder() {
  local name="$1"
  shift
  local value="${!name:-}"
  local placeholder
  for placeholder in "$@"; do
    if [ "$value" = "$placeholder" ]; then
      fail "${name} still uses placeholder value '${placeholder}'"
      return 0
    fi
  done
}

check_equal() {
  local left_name="$1"
  local right_name="$2"
  local label="$3"
  if [ "${!left_name:-}" != "${!right_name:-}" ]; then
    fail "${label} mismatch: ${left_name} and ${right_name} differ"
  fi
}

check_git_ignored() {
  local path="$1"
  if command -v git >/dev/null 2>&1; then
    if ! git -C "${ROOT_DIR}" check-ignore -q "$path"; then
      fail "secret-bearing file is not git-ignored: ${path}"
    fi
  fi
}

read_mode() {
  local path="$1"
  if stat -f '%Lp' "$path" >/dev/null 2>&1; then
    stat -f '%Lp' "$path"
    return 0
  fi
  if stat -c '%a' "$path" >/dev/null 2>&1; then
    stat -c '%a' "$path"
    return 0
  fi
  return 1
}

check_secret_file_mode() {
  local path="$1"
  local mode
  local last_three
  if ! mode="$(read_mode "$path")"; then
    warn "could not determine file mode for ${path}"
    return 0
  fi
  last_three="${mode: -3}"
  if [ "${#last_three}" -lt 3 ]; then
    last_three="$(printf '%03d' "${last_three}")"
  fi
  if [ "${last_three:1:1}" != "0" ] || [ "${last_three:2:1}" != "0" ]; then
    fail "file permissions are too open for ${path}: mode ${mode}, expected owner-only access such as 600"
  fi
}

check_min_length() {
  local name="$1"
  local min_len="$2"
  local value="${!name:-}"
  if [ -n "$value" ] && [ "${#value}" -lt "$min_len" ]; then
    warn "${name} is shorter than ${min_len} characters"
  fi
}

check_runtime_base_url() {
  local value="${AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL:-}"
  if [ -z "$value" ]; then
    fail "AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL is empty"
    return 0
  fi
  if ! printf '%s' "$value" | grep -Eq '^https?://[^/]+(:[0-9]+)?$'; then
    fail "AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL is not a simple http(s) base URL: ${value}"
  fi
  if printf '%s' "$value" | grep -Eq '://[^/]*_[^/]*(:|$)'; then
    fail "AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL uses an underscore hostname that Java URI parsing rejects: ${value}"
  fi
  if printf '%s' "$value" | grep -Eq '://(127\.0\.0\.1|localhost)(:|$)'; then
    fail "AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL points to localhost; Compose-internal backend calls should use the runtime service DNS name"
  fi
}

check_loopback_binding() {
  local name="$1"
  local value="${!name:-}"
  case "$value" in
    "" | 127.0.0.1 | localhost | ::1)
      ;;
    *)
      warn "${name} is not loopback-bound: ${value}"
      ;;
  esac
}

check_provider_requirements() {
  if [ "${AGENT_RUNTIME_PROVIDER:-}" = "openai_compatible" ]; then
    if [ -z "${OPENAI_COMPATIBLE_BASE_URL:-${OPENAI_BASE_URL:-}}" ]; then
      fail "openai_compatible provider requires OPENAI_COMPATIBLE_BASE_URL or OPENAI_BASE_URL"
    fi
    if [ -z "${OPENAI_COMPATIBLE_API_KEY:-${OPENAI_API_KEY:-}}" ]; then
      fail "openai_compatible provider requires OPENAI_COMPATIBLE_API_KEY or OPENAI_API_KEY"
    fi
  fi
}

check_optional_integrations() {
  if [ "${AICOOS_CONFIG_INTEGRATION_LINEAR_ENABLED:-false}" = "true" ]; then
    require_env AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY
    reject_placeholder AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY "change-this-linear-key" "replace-me"
  fi
  if [ "${AICOOS_CONFIG_INTEGRATION_FIGMA_ENABLED:-false}" = "true" ]; then
    require_env AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY
    reject_placeholder AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY "change-this-figma-key" "replace-me"
  fi
}

if [ ! -f "${COMPOSE_ENV_FILE}" ]; then
  printf '[error] pilot compose env file not found: %s\n' "${COMPOSE_ENV_FILE}" >&2
  printf '[hint] copy deploy/pilot/compose.env.example to deploy/pilot/compose.env first\n' >&2
  exit 1
fi

load_env_file "${COMPOSE_ENV_FILE}"

BACKEND_ENV_FILE="$(resolve_env_path "${BACKEND_ENV_FILE:-./backend.env}")"
AGENT_RUNTIME_ENV_FILE="$(resolve_env_path "${AGENT_RUNTIME_ENV_FILE:-./agent_runtime.env}")"

load_env_file "${BACKEND_ENV_FILE}"
load_env_file "${AGENT_RUNTIME_ENV_FILE}"

info "auditing pilot env files"
printf '  - %s\n' "${COMPOSE_ENV_FILE}"
printf '  - %s\n' "${BACKEND_ENV_FILE}"
printf '  - %s\n' "${AGENT_RUNTIME_ENV_FILE}"

check_git_ignored "${COMPOSE_ENV_FILE}"
check_git_ignored "${BACKEND_ENV_FILE}"
check_git_ignored "${AGENT_RUNTIME_ENV_FILE}"

check_secret_file_mode "${COMPOSE_ENV_FILE}"
check_secret_file_mode "${BACKEND_ENV_FILE}"
check_secret_file_mode "${AGENT_RUNTIME_ENV_FILE}"

require_env MYSQL_APP_PASSWORD
require_env MYSQL_ROOT_PASSWORD
require_env DB_USERNAME
require_env DB_PASSWORD
require_env APP_JWT_SECRET
require_env AICOOS_CONFIG_AGENT_RUNTIME_ENABLED
require_env AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL
require_env AICOOS_CONFIG_AGENT_RUNTIME_API_KEY
require_env AGENT_RUNTIME_API_KEY
require_env AGENT_RUNTIME_PROVIDER
require_env AGENT_RUNTIME_MODEL

reject_placeholder MYSQL_APP_PASSWORD "change-this-db-password" "aA123456"
reject_placeholder MYSQL_ROOT_PASSWORD "change-this-root-password" "aA123456"
reject_placeholder DB_PASSWORD "change-this-db-password" "aA123456"
reject_placeholder APP_JWT_SECRET "change-this-jwt-secret-with-strong-random-value" "replace-this-with-a-secure-env-secret"
reject_placeholder AICOOS_CONFIG_AGENT_RUNTIME_API_KEY "change-this-shared-runtime-key" "agent-runtime-local-key"
reject_placeholder AGENT_RUNTIME_API_KEY "change-this-shared-runtime-key" "agent-runtime-local-key"

check_min_length MYSQL_APP_PASSWORD 16
check_min_length MYSQL_ROOT_PASSWORD 16
check_min_length APP_JWT_SECRET 24
check_min_length AICOOS_CONFIG_AGENT_RUNTIME_API_KEY 24
check_min_length AGENT_RUNTIME_API_KEY 24

check_equal MYSQL_APP_USER DB_USERNAME "database application user"
check_equal MYSQL_APP_PASSWORD DB_PASSWORD "database application password"
check_equal AICOOS_CONFIG_AGENT_RUNTIME_API_KEY AGENT_RUNTIME_API_KEY "backend/runtime shared key"

if [ "${MYSQL_ROOT_PASSWORD}" = "${MYSQL_APP_PASSWORD}" ]; then
  warn "MYSQL_ROOT_PASSWORD matches MYSQL_APP_PASSWORD"
fi

check_runtime_base_url
check_provider_requirements
check_optional_integrations

check_loopback_binding PILOT_DB_BIND_HOST
check_loopback_binding PILOT_RUNTIME_BIND_HOST
check_loopback_binding PILOT_BACKEND_BIND_HOST

printf '\n== Pilot env audit summary ==\n'
if [ "${failures}" -gt 0 ]; then
  printf 'Found %s error(s) and %s warning(s).\n' "${failures}" "${warnings}" >&2
  exit 1
fi

printf 'Pilot env audit passed with %s warning(s).\n' "${warnings}"
exit 0
