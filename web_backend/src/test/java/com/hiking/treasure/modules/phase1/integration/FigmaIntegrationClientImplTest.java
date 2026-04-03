package com.hiking.treasure.modules.phase1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadRequest;
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

class FigmaIntegrationClientImplTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void readContextUsesTokenAndParsesFileAndNodeMetadata() throws Exception {
        AtomicReference<String> nodeQueryRef = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/files/demoFileKey", exchange -> {
            assertEquals("figma-test-key", exchange.getRequestHeaders().getFirst("X-Figma-Token"));
            assertTrue(exchange.getRequestHeaders().getFirst("X-Request-Id") != null
                    && !exchange.getRequestHeaders().getFirst("X-Request-Id").isBlank());
            byte[] response = """
                    {
                      "name": "AICoOS Product Design",
                      "lastModified": "2026-03-30T10:00:00Z",
                      "document": {
                        "id": "0:1",
                        "name": "Root"
                      }
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        server.createContext("/files/demoFileKey/nodes", exchange -> {
            assertEquals("figma-test-key", exchange.getRequestHeaders().getFirst("X-Figma-Token"));
            assertTrue(exchange.getRequestHeaders().getFirst("X-Request-Id") != null
                    && !exchange.getRequestHeaders().getFirst("X-Request-Id").isBlank());
            nodeQueryRef.set(exchange.getRequestURI().getQuery());
            byte[] response = """
                    {
                      "nodes": {
                        "123:456": {
                          "document": {
                            "id": "123:456",
                            "name": "Project Requirement Flow",
                            "type": "FRAME"
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
        when(systemConfigService.getBoolean(FigmaIntegrationClientImpl.CONFIG_ENABLED, false)).thenReturn(true);
        when(systemConfigService.getString(
                FigmaIntegrationClientImpl.CONFIG_API_BASE_URL,
                "https://api.figma.com/v1")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort());
        when(systemConfigService.getString(FigmaIntegrationClientImpl.CONFIG_API_KEY, "")).thenReturn("figma-test-key");

        FigmaIntegrationClientImpl client = new FigmaIntegrationClientImpl(systemConfigService, new ObjectMapper());
        FigmaContextReadRequest request = new FigmaContextReadRequest();
        request.setFileKey("demoFileKey");
        request.setNodeId("123-456");

        var response = client.readContext(request);

        assertEquals("demoFileKey", response.getFileKey());
        assertEquals("AICoOS Product Design", response.getFileName());
        assertEquals("123:456", response.getNodeId());
        assertEquals("Project Requirement Flow", response.getNodeName());
        assertEquals("https://www.figma.com/design/demoFileKey/context?node-id=123-456", response.getExternalUrl());
        assertTrue(
                nodeQueryRef.get().contains("ids=123%3A456")
                        || nodeQueryRef.get().contains("ids=123:456")
        );
    }

    @Test
    void readContextReturnsActionableMessageWhenNodeIsMissing() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/files/demoFileKey", exchange -> {
            byte[] response = """
                    {
                      "name": "AICoOS Product Design",
                      "lastModified": "2026-03-30T10:00:00Z"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        server.createContext("/files/demoFileKey/nodes", exchange -> {
            byte[] response = """
                    {
                      "nodes": {}
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
        when(systemConfigService.getBoolean(FigmaIntegrationClientImpl.CONFIG_ENABLED, false)).thenReturn(true);
        when(systemConfigService.getString(
                FigmaIntegrationClientImpl.CONFIG_API_BASE_URL,
                "https://api.figma.com/v1")).thenReturn("http://127.0.0.1:" + server.getAddress().getPort());
        when(systemConfigService.getString(FigmaIntegrationClientImpl.CONFIG_API_KEY, "")).thenReturn("figma-test-key");

        FigmaIntegrationClientImpl client = new FigmaIntegrationClientImpl(systemConfigService, new ObjectMapper());
        FigmaContextReadRequest request = new FigmaContextReadRequest();
        request.setFileKey("demoFileKey");
        request.setNodeId("123:456");

        BusinessException exception = assertThrows(BusinessException.class, () -> client.readContext(request));
        assertTrue(exception.getMessage().contains("未找到指定的 Figma 节点"));
    }
}
