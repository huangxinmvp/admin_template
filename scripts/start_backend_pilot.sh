#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/web_backend"
BACKEND_ENV_FILE="${BACKEND_ENV_FILE:-}"

load_env_file() {
  local env_file="$1"
  if [ -z "$env_file" ]; then
    return 0
  fi
  if [ ! -f "$env_file" ]; then
    printf '[error] backend env file not found: %s\n' "$env_file" >&2
    exit 1
  fi
  set -a
  # shellcheck disable=SC1090
  . "$env_file"
  set +a
}

require_env() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    printf '[error] required env is missing: %s\n' "$name" >&2
    exit 1
  fi
}

reject_placeholder() {
  local name="$1"
  shift
  local value="${!name:-}"
  local placeholder
  for placeholder in "$@"; do
    if [ "$value" = "$placeholder" ]; then
      printf '[error] %s still uses an insecure placeholder value\n' "$name" >&2
      exit 1
    fi
  done
}

load_env_file "$BACKEND_ENV_FILE"

if [ ! -d "${BACKEND_DIR}/target" ]; then
  printf '[error] backend target directory not found. Build it first with ./mvnw -q -DskipTests package\n' >&2
  exit 1
fi

if [ -z "${BACKEND_JAR_PATH:-}" ]; then
  BACKEND_JAR_PATH="$(find "${BACKEND_DIR}/target" -maxdepth 1 -type f -name '*.jar' ! -name '*original*.jar' | sort | tail -n 1)"
fi

if [ -z "${BACKEND_JAR_PATH:-}" ] || [ ! -f "${BACKEND_JAR_PATH}" ]; then
  printf '[error] backend jar not found. Build it first with ./mvnw -q -DskipTests package\n' >&2
  exit 1
fi

command -v java >/dev/null 2>&1 || {
  printf '[error] java command not found\n' >&2
  exit 1
}

require_env DB_URL
require_env DB_USERNAME
require_env DB_PASSWORD
require_env APP_JWT_SECRET
require_env AICOOS_CONFIG_AGENT_RUNTIME_BASE_URL
require_env AICOOS_CONFIG_AGENT_RUNTIME_API_KEY

reject_placeholder DB_PASSWORD "aA123456" "change-this-db-password"
reject_placeholder APP_JWT_SECRET "replace-this-with-a-secure-env-secret" "change-this-jwt-secret-with-strong-random-value"
reject_placeholder AICOOS_CONFIG_AGENT_RUNTIME_API_KEY "agent-runtime-local-key" "change-this-shared-runtime-key"

if [ "${AICOOS_CONFIG_INTEGRATION_LINEAR_ENABLED:-false}" = "true" ]; then
  require_env AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY
  reject_placeholder AICOOS_CONFIG_INTEGRATION_LINEAR_API_KEY "change-this-linear-key" "replace-me"
fi

if [ "${AICOOS_CONFIG_INTEGRATION_FIGMA_ENABLED:-false}" = "true" ]; then
  require_env AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY
  reject_placeholder AICOOS_CONFIG_INTEGRATION_FIGMA_API_KEY "change-this-figma-key" "replace-me"
fi

export SPRING_DEVTOOLS_RESTART_ENABLED=false

printf '[info] starting backend jar: %s\n' "${BACKEND_JAR_PATH}"
exec java -jar "${BACKEND_JAR_PATH}"
