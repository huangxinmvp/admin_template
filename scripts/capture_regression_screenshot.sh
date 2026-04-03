#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "usage: $0 <run-dir> <name> [interactive|fullscreen] [delay-seconds]" >&2
  exit 1
fi

RUN_DIR="$1"
NAME="$2"
MODE="${3:-interactive}"
DELAY="${4:-0}"

SCREENSHOT_DIR="${RUN_DIR}/screenshots"
mkdir -p "${SCREENSHOT_DIR}"

TARGET_FILE="${SCREENSHOT_DIR}/${NAME}.png"
ERROR_FILE="${SCREENSHOT_DIR}/${NAME}.capture-failed.txt"
ERR_TMP="$(mktemp)"
trap 'rm -f "${ERR_TMP}"' EXIT

CMD=(/usr/sbin/screencapture)

case "${MODE}" in
  interactive)
    CMD+=(-i)
    ;;
  fullscreen)
    CMD+=(-x)
    ;;
  *)
    echo "unsupported mode: ${MODE}" >&2
    exit 1
    ;;
esac

if [ "${DELAY}" != "0" ]; then
  CMD+=(-T "${DELAY}")
fi

if ! command -v /usr/sbin/screencapture >/dev/null 2>&1; then
  {
    echo "capture_failed=true"
    echo "reason=screencapture_unavailable"
    echo "created_at=$(date '+%Y-%m-%d %H:%M:%S %Z')"
  } > "${ERROR_FILE}"
  printf '%s\n' "${ERROR_FILE}"
  exit 0
fi

if "${CMD[@]}" "${TARGET_FILE}" 2>"${ERR_TMP}"; then
  printf '%s\n' "${TARGET_FILE}"
  exit 0
fi

{
  echo "capture_failed=true"
  echo "mode=${MODE}"
  echo "delay=${DELAY}"
  echo "created_at=$(date '+%Y-%m-%d %H:%M:%S %Z')"
  echo "stderr<<EOF"
  cat "${ERR_TMP}"
  echo "EOF"
} > "${ERROR_FILE}"

printf '%s\n' "${ERROR_FILE}"
