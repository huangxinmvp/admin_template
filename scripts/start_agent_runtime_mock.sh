#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

export AGENT_RUNTIME_API_KEY="${AGENT_RUNTIME_API_KEY:-agent-runtime-local-key}"
export AGENT_RUNTIME_PROVIDER="${AGENT_RUNTIME_PROVIDER:-mock}"
export AGENT_RUNTIME_MODEL="${AGENT_RUNTIME_MODEL:-mock-suggestion-v1}"
export AGENT_RUNTIME_TIMEOUT_SECONDS="${AGENT_RUNTIME_TIMEOUT_SECONDS:-20}"
export AGENT_RUNTIME_ALLOW_MOCK_FALLBACK="${AGENT_RUNTIME_ALLOW_MOCK_FALLBACK:-true}"

HOST="${AGENT_RUNTIME_HOST:-127.0.0.1}"
PORT="${AGENT_RUNTIME_PORT:-8091}"

cd "${ROOT_DIR}"
exec python3 -m uvicorn agent_runtime.app:app --host "${HOST}" --port "${PORT}"
