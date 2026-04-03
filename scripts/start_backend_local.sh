#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/web_backend"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-admin_template}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-aA123456}"

export DB_URL="${DB_URL:-jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}"
export DB_USERNAME="${DB_USERNAME:-root}"
export DB_PASSWORD="${DB_PASSWORD:-${MYSQL_ROOT_PASSWORD}}"
export SPRING_DEVTOOLS_RESTART_ENABLED="${SPRING_DEVTOOLS_RESTART_ENABLED:-false}"

cd "${BACKEND_DIR}"
exec ./mvnw spring-boot:run
