from __future__ import annotations

import os
import unittest

from fastapi.testclient import TestClient

from agent_runtime.app import create_app


class AgentRuntimeAppTest(unittest.TestCase):
    def setUp(self) -> None:
        os.environ["AGENT_RUNTIME_API_KEY"] = "test-key"
        os.environ["AGENT_RUNTIME_PROVIDER"] = "mock"
        os.environ["AGENT_RUNTIME_MODEL"] = "mock-suggestion-v1"
        os.environ["AGENT_RUNTIME_ALLOW_MOCK_FALLBACK"] = "true"
        os.environ.pop("OPENAI_BASE_URL", None)
        os.environ.pop("OPENAI_API_KEY", None)
        os.environ.pop("OPENAI_COMPATIBLE_BASE_URL", None)
        os.environ.pop("OPENAI_COMPATIBLE_API_KEY", None)
        self.client = TestClient(create_app())

    def tearDown(self) -> None:
        for key in [
            "AGENT_RUNTIME_API_KEY",
            "AGENT_RUNTIME_PROVIDER",
            "AGENT_RUNTIME_MODEL",
            "AGENT_RUNTIME_ALLOW_MOCK_FALLBACK",
            "OPENAI_BASE_URL",
            "OPENAI_API_KEY",
            "OPENAI_COMPATIBLE_BASE_URL",
            "OPENAI_COMPATIBLE_API_KEY",
        ]:
            os.environ.pop(key, None)

    def test_health(self) -> None:
        response = self.client.get("/health")
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual("ok", body["status"])
        self.assertTrue(body["runtimeKeyConfigured"])
        self.assertTrue(body["defaultProviderReady"])
        self.assertIsNotNone(response.headers.get("X-Request-Id"))

    def test_request_id_is_echoed_back(self) -> None:
        response = self.client.post(
            "/api/v1/clarifications/generate",
            headers={
                "X-Agent-Runtime-Key": "test-key",
                "X-Request-Id": "req-test-123",
            },
            json={
                "projectName": "AICoOS",
                "businessGoal": "",
            },
        )

        self.assertEqual(200, response.status_code)
        self.assertEqual("req-test-123", response.headers.get("X-Request-Id"))

    def test_health_reports_minimal_readiness_shape_without_exposing_provider_config(self) -> None:
        os.environ["AGENT_RUNTIME_PROVIDER"] = "openai_compatible"
        os.environ["OPENAI_BASE_URL"] = "https://provider.example.test/v1"
        os.environ["OPENAI_API_KEY"] = "provider-secret-key"
        client = TestClient(create_app())

        response = client.get("/health")

        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual("ok", body["status"])
        self.assertTrue(body["defaultProviderReady"])
        self.assertNotIn("provider", body)
        self.assertNotIn("model", body)
        self.assertNotIn("openaiCompatibleConfigured", body)
        self.assertNotIn("openaiCompatibleBaseUrl", body)
        self.assertNotIn("provider-secret-key", response.text)

    def test_health_returns_degraded_when_openai_provider_is_not_configured(self) -> None:
        os.environ["AGENT_RUNTIME_PROVIDER"] = "openai_compatible"
        client = TestClient(create_app())

        response = client.get("/health")

        self.assertEqual(503, response.status_code)
        body = response.json()
        self.assertEqual("degraded", body["status"])
        self.assertFalse(body["defaultProviderReady"])

    def test_generate_clarifications_requires_key(self) -> None:
        response = self.client.post(
            "/api/v1/clarifications/generate",
            json={},
        )
        self.assertEqual(401, response.status_code)

    def test_generate_clarifications_returns_structured_suggestions(self) -> None:
        response = self.client.post(
            "/api/v1/clarifications/generate",
            headers={"X-Agent-Runtime-Key": "test-key"},
            json={
                "projectName": "AICoOS",
                "featureSummary": "",
                "businessGoal": "",
            },
        )
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual("mock", body["provider"])
        self.assertTrue(body["suggestions"])
        first = body["suggestions"][0]
        self.assertIn("title", first)
        self.assertIn("blockerFlag", first)

    def test_requirement_clarification_review_returns_participants_and_suggestions(self) -> None:
        response = self.client.post(
            "/api/v1/collaboration/requirement-clarification-review",
            headers={"X-Agent-Runtime-Key": "test-key"},
            json={
                "projectName": "AICoOS",
                "businessGoal": "",
                "featureSummary": "",
            },
        )
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual(["Requirement Analyst", "Product Manager"], body["participants"])
        self.assertTrue(body["suggestions"])
        self.assertTrue(body["nextQuestions"])
        self.assertIn("recommendedOperatorAction", body)
        self.assertIn("governanceInterpretation", body)
        self.assertIn("decisionEscalationAdvised", body)
        self.assertIn("followUpHints", body)
        self.assertTrue(body["roleInsights"])
        self.assertIn("governanceReason", body["suggestions"][0])

    def test_decision_budget_review_returns_structured_suggestion(self) -> None:
        response = self.client.post(
            "/api/v1/collaboration/decision-budget-review",
            headers={"X-Agent-Runtime-Key": "test-key"},
            json={
                "projectName": "AICoOS",
                "decisionTitle": "确认首期范围",
                "decisionType": "scope_change",
                "decisionDescription": "需要确认 MVP 范围",
                "blockerFlag": True,
            },
        )
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual(["Product Manager", "Budget Analyst"], body["participants"])
        self.assertIn("suggestion", body)
        self.assertIn("budgetConfirmationAdvised", body["suggestion"])
        self.assertIn("decisionLinkageSummary", body)
        self.assertIn("budgetLinkageSummary", body)
        self.assertIn("followUpHints", body)
        self.assertIn("roleInsights", body)

    def test_product_architecture_brief_returns_risks_and_next_steps(self) -> None:
        response = self.client.post(
            "/api/v1/collaboration/product-architecture-brief",
            headers={"X-Agent-Runtime-Key": "test-key"},
            json={
                "projectName": "AICoOS",
                "featureSummary": "治理型软件交付控制台",
                "focusNotes": "优先考虑 MVP 范围和系统边界",
            },
        )
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual(["Product Manager", "Architect"], body["participants"])
        self.assertTrue(body["suggestion"]["risks"])
        self.assertTrue(body["suggestion"]["nextSteps"])
        self.assertIn("projectGovernanceLinkageSummary", body)
        self.assertIn("followUpHints", body)
        self.assertIn("roleInsights", body)

    def test_invalid_meeting_payload_rejected(self) -> None:
        response = self.client.post(
            "/api/v1/meetings/summarize",
            headers={"X-Agent-Runtime-Key": "test-key"},
            json={"rawNotes": ""},
        )
        self.assertEqual(422, response.status_code)

    def test_meeting_summary_returns_candidates(self) -> None:
        response = self.client.post(
            "/api/v1/meetings/summarize",
            headers={"X-Agent-Runtime-Key": "test-key"},
            json={
                "projectName": "AICoOS",
                "meetingTitle": "需求评审",
                "rawNotes": "需要确认首期范围\n预算需要再评估\nTODO: 更新决策项",
            },
        )
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertIn("suggestion", body)
        self.assertTrue(body["suggestion"]["actionItems"])


if __name__ == "__main__":
    unittest.main()
