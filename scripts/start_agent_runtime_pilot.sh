#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_ENV_FILE="${AGENT_RUNTIME_ENV_FILE:-}"

load_env_file() {
  local env_file="$1"
  if [ -z "$env_file" ]; then
    return 0
  fi
  if [ ! -f "$env_file" ]; then
    printf '[error] runtime env file not found: %s\n' "$env_file" >&2
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

load_env_file "$RUNTIME_ENV_FILE"

PYTHON_BIN="${AGENT_RUNTIME_PYTHON_BIN:-${ROOT_DIR}/agent_runtime/.venv/bin/python}"
HOST="${AGENT_RUNTIME_HOST:-127.0.0.1}"
PORT="${AGENT_RUNTIME_PORT:-8091}"

if [ ! -x "$PYTHON_BIN" ]; then
  printf '[error] runtime python interpreter not executable: %s\n' "$PYTHON_BIN" >&2
  exit 1
fi

require_env AGENT_RUNTIME_API_KEY
require_env AGENT_RUNTIME_PROVIDER
require_env AGENT_RUNTIME_MODEL

reject_placeholder AGENT_RUNTIME_API_KEY "agent-runtime-local-key" "change-this-shared-runtime-key"

if [ "${AGENT_RUNTIME_PROVIDER}" = "openai_compatible" ]; then
  if [ -z "${OPENAI_COMPATIBLE_BASE_URL:-${OPENAI_BASE_URL:-}}" ]; then
    printf '[error] openai-compatible provider requires OPENAI_COMPATIBLE_BASE_URL or OPENAI_BASE_URL\n' >&2
    exit 1
  fi
  if [ -z "${OPENAI_COMPATIBLE_API_KEY:-${OPENAI_API_KEY:-}}" ]; then
    printf '[error] openai-compatible provider requires OPENAI_COMPATIBLE_API_KEY or OPENAI_API_KEY\n' >&2
    exit 1
  fi
fi

export AGENT_RUNTIME_ALLOW_MOCK_FALLBACK="${AGENT_RUNTIME_ALLOW_MOCK_FALLBACK:-false}"

printf '[info] starting agent_runtime on %s:%s with provider=%s\n' "$HOST" "$PORT" "${AGENT_RUNTIME_PROVIDER}"
cd "${ROOT_DIR}"
exec "${PYTHON_BIN}" -m uvicorn agent_runtime.app:app --host "${HOST}" --port "${PORT}"
