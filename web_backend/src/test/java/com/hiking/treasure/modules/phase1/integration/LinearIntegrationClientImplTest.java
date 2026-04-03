package com.hiking.treasure.modules.phase1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateRequest;
import com.hiking.treasure.service.SystemConfigService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LinearIntegrationClientImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createIssueUsesConfiguredHeadersAndParsesResponse() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            assertEquals("linear-test-key", exchange.getRequestHeaders().getFirst("Authorization"));
            assertTrue(exchange.getRequestHeaders().getFirst("X-Request-Id") != null
                    && !exchange.getRequestHeaders().getFirst("X-Request-Id").isBlank());
            requestBodyRef.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {
                      "data": {
                        "issueCreate": {
                          "success": true,
                          "issue": {
                            "id": "linear-issue-1",
                            "identifier": "AIC-101",
                            "title": "AICoOS Project Mirror",
                            "url": "https://linear.app/acme/issue/AIC-101"
                          }
                        }
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
        when(systemConfigService.getBoolean(LinearIntegrationClientImpl.CONFIG_ENABLED, false)).thenReturn(true);
        when(systemConfigService.getString(
                LinearIntegrationClientImpl.CONFIG_GRAPHQL_URL,
                "https://api.linear.app/graphql")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/graphql");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_API_KEY, "")).thenReturn("linear-test-key");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_DEFAULT_TEAM_ID, "")).thenReturn("team-default");

        LinearIntegrationClientImpl client = new LinearIntegrationClientImpl(systemConfigService, objectMapper);
        LinearIssueCreateRequest request = new LinearIssueCreateRequest();
        request.setTitle("AICoOS Project Mirror");
        request.setDescription("Controlled sync from AICoOS.");

        var response = client.createIssue(request);

        assertEquals("linear-issue-1", response.getId());
        assertEquals("AIC-101", response.getIdentifier());
        assertEquals("https://linear.app/acme/issue/AIC-101", response.getUrl());

        JsonNode root = objectMapper.readTree(requestBodyRef.get());
        assertTrue(root.path("query").asText().contains("issueCreate"));
        assertEquals("team-default", root.path("variables").path("input").path("teamId").asText());
        assertEquals("AICoOS Project Mirror", root.path("variables").path("input").path("title").asText());
    }

    @Test
    void createCommentUsesIssueIdAndParsesResponse() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            assertEquals("linear-test-key", exchange.getRequestHeaders().getFirst("Authorization"));
            assertTrue(exchange.getRequestHeaders().getFirst("X-Request-Id") != null
                    && !exchange.getRequestHeaders().getFirst("X-Request-Id").isBlank());
            requestBodyRef.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {
                      "data": {
                        "commentCreate": {
                          "success": true,
                          "comment": {
                            "id": "linear-comment-1",
                            "body": "AICoOS clarification comment",
                            "url": "https://linear.app/acme/comment/1"
                          }
                        }
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
        when(systemConfigService.getBoolean(LinearIntegrationClientImpl.CONFIG_ENABLED, false)).thenReturn(true);
        when(systemConfigService.getString(
                LinearIntegrationClientImpl.CONFIG_GRAPHQL_URL,
                "https://api.linear.app/graphql")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/graphql");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_API_KEY, "")).thenReturn("linear-test-key");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_DEFAULT_TEAM_ID, "")).thenReturn("team-default");

        LinearIntegrationClientImpl client = new LinearIntegrationClientImpl(systemConfigService, objectMapper);
        LinearCommentCreateRequest request = new LinearCommentCreateRequest();
        request.setIssueId("linear-issue-1");
        request.setBody("AICoOS clarification comment");

        var response = client.createComment(request);

        assertEquals("linear-comment-1", response.getId());
        assertEquals("https://linear.app/acme/comment/1", response.getUrl());

        JsonNode root = objectMapper.readTree(requestBodyRef.get());
        assertTrue(root.path("query").asText().contains("commentCreate"));
        assertEquals("linear-issue-1", root.path("variables").path("input").path("issueId").asText());
    }

    @Test
    void createIssueReturnsActionableMessageWhenUnauthorized() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            byte[] response = """
                    {
                      "message": "Unauthorized"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        server.start();

        SystemConfigService systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.getBoolean(LinearIntegrationClientImpl.CONFIG_ENABLED, false)).thenReturn(true);
        when(systemConfigService.getString(
                LinearIntegrationClientImpl.CONFIG_GRAPHQL_URL,
                "https://api.linear.app/graphql")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/graphql");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_API_KEY, "")).thenReturn("linear-test-key");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_DEFAULT_TEAM_ID, "")).thenReturn("team-default");

        LinearIntegrationClientImpl client = new LinearIntegrationClientImpl(systemConfigService, objectMapper);
        LinearIssueCreateRequest request = new LinearIssueCreateRequest();
        request.setTitle("Unauthorized check");

        BusinessException exception = assertThrows(BusinessException.class, () -> client.createIssue(request));
        assertTrue(exception.getMessage().contains("integration.linear.apiKey"));
    }

    @Test
    void createIssueReturnsActionableMessageWhenApiKeyIsSentAsBearerToken() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/graphql", exchange -> {
            byte[] response = """
                    {
                      "errors": [
                        {
                          "message": "It looks like you're trying to use an API key as a Bearer token. Remove the Bearer prefix from the Authorization header."
                        }
                      ]
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        server.start();

        SystemConfigService systemConfigService = mock(SystemConfigService.class);
        when(systemConfigService.getBoolean(LinearIntegrationClientImpl.CONFIG_ENABLED, false)).thenReturn(true);
        when(systemConfigService.getString(
                LinearIntegrationClientImpl.CONFIG_GRAPHQL_URL,
                "https://api.linear.app/graphql")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort() + "/graphql");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_API_KEY, "")).thenReturn("linear-test-key");
        when(systemConfigService.getString(LinearIntegrationClientImpl.CONFIG_DEFAULT_TEAM_ID, "")).thenReturn("team-default");

        LinearIntegrationClientImpl client = new LinearIntegrationClientImpl(systemConfigService, objectMapper);
        LinearIssueCreateRequest request = new LinearIssueCreateRequest();
        request.setTitle("Bearer misuse check");

        BusinessException exception = assertThrows(BusinessException.class, () -> client.createIssue(request));
        assertTrue(exception.getMessage().contains("不要添加 Bearer 前缀"));
    }
}
