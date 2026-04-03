from __future__ import annotations

import os
import unittest

from fastapi.testclient import TestClient

from agent_runtime.app import create_app


class AgentRuntimeDiagnosticsTest(unittest.TestCase):
    def setUp(self) -> None:
        os.environ["AGENT_RUNTIME_API_KEY"] = "test-key"
        os.environ["AGENT_RUNTIME_PROVIDER"] = "mock"
        os.environ["AGENT_RUNTIME_MODEL"] = "mock-suggestion-v1"
        self.client = TestClient(create_app())

    def tearDown(self) -> None:
        for key in [
            "AGENT_RUNTIME_API_KEY",
            "AGENT_RUNTIME_PROVIDER",
            "AGENT_RUNTIME_MODEL",
        ]:
            os.environ.pop(key, None)

    def test_health_diagnostics_requires_runtime_key(self) -> None:
        response = self.client.get("/health/diagnostics")
        self.assertEqual(401, response.status_code)

    def test_health_diagnostics_reports_endpoint_counts(self) -> None:
        self.client.get("/health")
        self.client.post(
            "/api/v1/clarifications/generate",
            headers={"X-Agent-Runtime-Key": "test-key", "X-Request-Id": "diag-123"},
            json={"projectName": "AICoOS", "businessGoal": ""},
        )

        response = self.client.get(
            "/health/diagnostics",
            headers={"X-Agent-Runtime-Key": "test-key", "X-Request-Id": "diag-check-1"},
        )

        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual("diag-check-1", body["requestId"])
        self.assertIn("/health", body["operations"])
        self.assertIn("/api/v1/clarifications/generate", body["operations"])
        self.assertGreaterEqual(body["operations"]["/health"]["successCount"], 1)
        self.assertGreaterEqual(body["operations"]["/api/v1/clarifications/generate"]["successCount"], 1)
