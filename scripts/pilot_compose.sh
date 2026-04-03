#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${PILOT_COMPOSE_FILE:-${ROOT_DIR}/deploy/pilot/compose.yaml}"
COMPOSE_ENV_FILE="${PILOT_COMPOSE_ENV_FILE:-${ROOT_DIR}/deploy/pilot/compose.env}"
BACKEND_DIR="${ROOT_DIR}/web_backend"
BACKEND_JAR="${BACKEND_DIR}/target/admin-template-0.0.1-SNAPSHOT.jar"
ENV_AUDIT_SCRIPT="${ROOT_DIR}/scripts/pilot_env_audit.sh"

should_package_backend() {
  local subcommand="${1:-}"
  shift || true
  case "${subcommand}" in
    build)
      return 0
      ;;
    up)
      local arg
      for arg in "$@"; do
        if [ "${arg}" = "--build" ]; then
          return 0
        fi
      done
      return 1
      ;;
    *)
      return 1
      ;;
  esac
}

should_audit_env() {
  local subcommand="${1:-}"
  case "${subcommand}" in
    config | build | up)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

audit_env_if_needed() {
  if [ "${PILOT_SKIP_ENV_AUDIT:-0}" = "1" ]; then
    printf '[info] skipping pilot env audit because PILOT_SKIP_ENV_AUDIT=1\n'
    return 0
  fi

  if [ ! -x "${ENV_AUDIT_SCRIPT}" ]; then
    printf '[error] pilot env audit script not found or not executable: %s\n' "${ENV_AUDIT_SCRIPT}" >&2
    exit 1
  fi

  "${ENV_AUDIT_SCRIPT}"
}

package_backend_if_needed() {
  if [ "${PILOT_SKIP_BACKEND_PACKAGE:-0}" = "1" ]; then
    printf '[info] skipping backend package step because PILOT_SKIP_BACKEND_PACKAGE=1\n'
    return 0
  fi

  if [ ! -x "${BACKEND_DIR}/mvnw" ]; then
    printf '[error] Maven wrapper not found: %s\n' "${BACKEND_DIR}/mvnw" >&2
    exit 1
  fi

  printf '[info] packaging backend jar for pilot image build\n'
  (
    cd "${BACKEND_DIR}"
    ./mvnw -q -DskipTests package
  )

  if [ ! -f "${BACKEND_JAR}" ]; then
    printf '[error] expected backend jar not found after package: %s\n' "${BACKEND_JAR}" >&2
    exit 1
  fi
}

if ! command -v docker >/dev/null 2>&1; then
  printf '[error] docker command not found\n' >&2
  exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
  printf '[error] pilot compose file not found: %s\n' "${COMPOSE_FILE}" >&2
  exit 1
fi

if [ ! -f "${COMPOSE_ENV_FILE}" ]; then
  printf '[error] pilot compose env file not found: %s\n' "${COMPOSE_ENV_FILE}" >&2
  printf '[hint] copy deploy/pilot/compose.env.example to deploy/pilot/compose.env first\n' >&2
  exit 1
fi

if should_audit_env "$@"; then
  audit_env_if_needed
fi

if should_package_backend "$@"; then
  package_backend_if_needed
fi

exec docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" "$@"
