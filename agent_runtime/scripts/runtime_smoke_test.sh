#!/usr/bin/env bash

set -euo pipefail

RUNTIME_BASE_URL="${RUNTIME_BASE_URL:-http://127.0.0.1:8091}"
RUNTIME_API_KEY="${RUNTIME_API_KEY:-agent-runtime-local-key}"

echo "[1/2] Health check: ${RUNTIME_BASE_URL}/health"
curl -fsS "${RUNTIME_BASE_URL}/health" | python3 -m json.tool

echo
echo "[2/2] Clarification suggestion smoke call"
curl -fsS \
  -H "Content-Type: application/json" \
  -H "X-Agent-Runtime-Key: ${RUNTIME_API_KEY}" \
  -X POST "${RUNTIME_BASE_URL}/api/v1/clarifications/generate" \
  -d '{
    "projectName": "AICoOS Demo",
    "projectType": "saas_platform",
    "businessGoal": "Validate local runtime integration",
    "featureSummary": "Need a structured clarification preview for a vague enterprise software request.",
    "timelineExpectation": "4 weeks",
    "budgetRange": "mid",
    "technicalConstraints": "Keep governance in the main system"
  }' | python3 -m json.tool
