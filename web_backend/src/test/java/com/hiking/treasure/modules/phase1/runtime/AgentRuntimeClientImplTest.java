package com.hiking.treasure.modules.phase1.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.ClarificationGenerateRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.ClarificationGenerateResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.DecisionBudgetReviewRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.DecisionBudgetReviewResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.RequestMeta;
import com.hiking.treasure.service.SystemConfigService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentRuntimeClientImplTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void generateClarificationSuggestionsSendsSharedKeyAndParsesResponse() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/clarifications/generate", exchange -> {
            assertEquals("runtime-test-key", exchange.getRequestHeaders().getFirst("X-Agent-Runtime-Key"));
            assertNotNull(exchange.getRequestHeaders().getFirst("X-Request-Id"));
            byte[] response = """
                    {
                      "provider":"mock",
                      "model":"mock-suggestion-v1",
                      "suggestions":[
                        {
                          "title":"明确业务目标边界",
                          "question":"最核心的业务目标是什么？",
                          "category":"business_goal",
                          "severity":"blocker",
                          "suggestedOptions":"提升转化率 / 降本增效",
                          "blockerFlag":true,
                          "reason":"目标缺失会阻塞后续估算"
                        }
                      ]
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        server.start();

        SystemConfigService systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.getBoolean(AgentRuntimeClientImpl.CONFIG_ENABLED, true)).thenReturn(true);
        when(systemConfigService.getString(
                AgentRuntimeClientImpl.CONFIG_BASE_URL,
                "http://127.0.0.1:8091")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort());
        when(systemConfigService.getString(
                AgentRuntimeClientImpl.CONFIG_API_KEY,
                "agent-runtime-local-key")).thenReturn("runtime-test-key");
        when(systemConfigService.getInt(AgentRuntimeClientImpl.CONFIG_TIMEOUT_MS, 5000)).thenReturn(3000);
        when(systemConfigService.getBoolean(AgentRuntimeClientImpl.CONFIG_MOCK_MODE, true)).thenReturn(true);
        when(systemConfigService.getString(AgentRuntimeClientImpl.CONFIG_MODEL, "mock-suggestion-v1"))
                .thenReturn("mock-suggestion-v1");

        AgentRuntimeClientImpl client = new AgentRuntimeClientImpl(systemConfigService, new ObjectMapper());
        ClarificationGenerateRequest request = new ClarificationGenerateRequest();
        request.setProjectName("AICoOS");
        request.setMeta(new RequestMeta());

        ClarificationGenerateResponse response = client.generateClarificationSuggestions(request);

        assertEquals("mock", response.getProvider());
        assertEquals("mock-suggestion-v1", response.getModel());
        assertNotNull(response.getSuggestions());
        assertEquals(1, response.getSuggestions().size());
        assertEquals("明确业务目标边界", response.getSuggestions().get(0).getTitle());
        assertEquals("mock", request.getMeta().getProviderHint());
    }

    @Test
    void generateDecisionBudgetReviewParsesStructuredResponse() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/collaboration/decision-budget-review", exchange -> {
            assertEquals("runtime-test-key", exchange.getRequestHeaders().getFirst("X-Agent-Runtime-Key"));
            assertNotNull(exchange.getRequestHeaders().getFirst("X-Request-Id"));
            byte[] response = """
                    {
                      "provider":"mock",
                      "model":"mock-suggestion-v1",
                      "participants":["Product Manager","Budget Analyst"],
                      "suggestion":{
                        "decisionRecommendation":"建议先锁定范围边界后再推进。",
                        "budgetImpactNote":"建议预留额外预算缓冲。",
                        "budgetConfirmationAdvised":true,
                        "recommendedOption":"优先采用更小范围方案",
                        "projectImpactNote":"可以降低后续返工风险。",
                        "blockerAssessment":"若不确认预算缓冲，后续风险较高。",
                        "nextSteps":["补齐预算确认","更新决策备注"]
                      }
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        server.start();

        SystemConfigService systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.getBoolean(AgentRuntimeClientImpl.CONFIG_ENABLED, true)).thenReturn(true);
        when(systemConfigService.getString(
                AgentRuntimeClientImpl.CONFIG_BASE_URL,
                "http://127.0.0.1:8091")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort());
        when(systemConfigService.getString(
                AgentRuntimeClientImpl.CONFIG_API_KEY,
                "agent-runtime-local-key")).thenReturn("runtime-test-key");
        when(systemConfigService.getInt(AgentRuntimeClientImpl.CONFIG_TIMEOUT_MS, 5000)).thenReturn(3000);
        when(systemConfigService.getBoolean(AgentRuntimeClientImpl.CONFIG_MOCK_MODE, true)).thenReturn(true);
        when(systemConfigService.getString(AgentRuntimeClientImpl.CONFIG_MODEL, "mock-suggestion-v1"))
                .thenReturn("mock-suggestion-v1");

        AgentRuntimeClientImpl client = new AgentRuntimeClientImpl(systemConfigService, new ObjectMapper());
        DecisionBudgetReviewRequest request = new DecisionBudgetReviewRequest();
        request.setDecisionTitle("确认首期范围");
        request.setMeta(new RequestMeta());

        DecisionBudgetReviewResponse response = client.generateDecisionBudgetReview(request);

        assertEquals("mock", response.getProvider());
        assertEquals(2, response.getParticipants().size());
        assertEquals("建议先锁定范围边界后再推进。", response.getSuggestion().getDecisionRecommendation());
        assertEquals("mock", request.getMeta().getProviderHint());
    }
}
