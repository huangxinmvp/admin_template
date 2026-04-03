#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${PILOT_COMPOSE_FILE:-${ROOT_DIR}/deploy/pilot/compose.yaml}"
COMPOSE_ENV_FILE="${PILOT_COMPOSE_ENV_FILE:-${ROOT_DIR}/deploy/pilot/compose.env}"
CREATE_SQL="${ROOT_DIR}/web_backend/src/main/resources/static/sql/create.sql"
SEED_SQL="${ROOT_DIR}/web_backend/src/main/resources/static/sql/seed-saas-admin.sql"
PERMISSION_SQL="${ROOT_DIR}/web_backend/src/main/resources/static/sql/bootstrap-template-permissions.sql"
PHASE1_DEMO_SEED_SQL="${ROOT_DIR}/web_backend/src/main/resources/static/sql/seed-aicoos-demo.sql"

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

if [ ! -f "${CREATE_SQL}" ]; then
  printf '[error] base schema SQL not found: %s\n' "${CREATE_SQL}" >&2
  exit 1
fi

if [ ! -f "${SEED_SQL}" ]; then
  printf '[error] admin seed SQL not found: %s\n' "${SEED_SQL}" >&2
  exit 1
fi

if [ ! -f "${PERMISSION_SQL}" ]; then
  printf '[error] permission bootstrap SQL not found: %s\n' "${PERMISSION_SQL}" >&2
  exit 1
fi

if [ ! -f "${PHASE1_DEMO_SEED_SQL}" ]; then
  printf '[error] phase1 demo seed SQL not found: %s\n' "${PHASE1_DEMO_SEED_SQL}" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
. "${COMPOSE_ENV_FILE}"
set +a

MYSQL_DATABASE="${MYSQL_DATABASE:-admin_template}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"

if [ -z "${MYSQL_ROOT_PASSWORD}" ]; then
  printf '[error] MYSQL_ROOT_PASSWORD is required in %s\n' "${COMPOSE_ENV_FILE}" >&2
  exit 1
fi

compose_mysql() {
  docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" exec -T mysql "$@"
}

if ! docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" ps -q mysql >/dev/null 2>&1; then
  printf '[error] mysql service is not defined in the pilot compose stack\n' >&2
  exit 1
fi

if [ -z "$(docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" ps -q mysql)" ]; then
  printf '[error] mysql service is not running. start the pilot stack first.\n' >&2
  exit 1
fi

table_exists="$(
  compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -N -s -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${MYSQL_DATABASE}' AND table_name='sys_user';"
)"

if [ "${table_exists}" = "0" ]; then
  printf '[info] base admin schema missing in %s; importing create.sql\n' "${MYSQL_DATABASE}"
  compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "${CREATE_SQL}"
else
  printf '[info] base admin schema already present in %s; skipping create.sql\n' "${MYSQL_DATABASE}"
fi

printf '[info] importing repeatable admin seed into %s\n' "${MYSQL_DATABASE}"
compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "${SEED_SQL}"

printf '[info] importing repeatable permission bootstrap into %s\n' "${MYSQL_DATABASE}"
compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "${PERMISSION_SQL}"

printf '[info] importing repeatable phase1 demo seed into %s\n' "${MYSQL_DATABASE}"
compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "${PHASE1_DEMO_SEED_SQL}"

admin_exists="$(
  compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -N -s -e \
    "SELECT COUNT(*) FROM ${MYSQL_DATABASE}.sys_user WHERE username='admin' AND del_flag=0;"
)"

if [ "${admin_exists}" != "1" ]; then
  printf '[error] expected seeded admin user was not found after bootstrap\n' >&2
  exit 1
fi

demo_project_exists="$(
  compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -N -s -e \
    "SELECT COUNT(*) FROM ${MYSQL_DATABASE}.ai_project WHERE id='demo_project_alpha';"
)"

demo_decision_exists="$(
  compose_mysql mysql -uroot "-p${MYSQL_ROOT_PASSWORD}" -N -s -e \
    "SELECT COUNT(*) FROM ${MYSQL_DATABASE}.ai_decision_item WHERE id='demo_decision_alpha_01';"
)"

if [ "${demo_project_exists}" != "1" ] || [ "${demo_decision_exists}" != "1" ]; then
  printf '[error] expected demo governance records were not found after bootstrap\n' >&2
  exit 1
fi

printf '[ok] pilot DB bootstrap completed for schema %s\n' "${MYSQL_DATABASE}"
printf '[info] default seeded login: admin / Admin@123456\n'
