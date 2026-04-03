package com.hiking.treasure.modules.phase1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.diagnostics.IntegrationDiagnostics;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.util.OutboundRequestSupport;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadResponse;
import com.hiking.treasure.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaIntegrationClientImpl implements FigmaIntegrationClient {

    static final String CONFIG_ENABLED = "integration.figma.enabled";
    static final String CONFIG_API_BASE_URL = "integration.figma.apiBaseUrl";
    static final String CONFIG_API_KEY = "integration.figma.apiKey";
    static final String CONFIG_TIMEOUT_MS = "integration.figma.timeoutMs";
    static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int MAX_ATTEMPTS = 2;

    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;

    @Override
    public FigmaContextReadResponse readContext(FigmaContextReadRequest request) {
        FigmaConfig config = resolveConfig();
        String fileKey = trim(request.getFileKey());
        if (fileKey == null || fileKey.isBlank()) {
            throw new BusinessException(400, "Figma File Key 未提供");
        }
        long startedAt = System.nanoTime();
        String requestId = OutboundRequestSupport.currentRequestIdOrGenerate();
        try {
            JsonNode fileNode = get(config, "/files/" + fileKey);
            FigmaContextReadResponse response = new FigmaContextReadResponse();
            response.setFileKey(fileKey);
            response.setFileName(fileNode.path("name").asText(null));
            response.setLastModifiedAt(fileNode.path("lastModified").asText(null));
            response.setExternalUrl("https://www.figma.com/file/" + fileKey);

            String nodeId = normalizeNodeId(request.getNodeId());
            if (nodeId != null && !nodeId.isBlank()) {
                JsonNode nodesNode = get(config, "/files/" + fileKey + "/nodes?ids=" + URLEncoder.encode(nodeId, StandardCharsets.UTF_8));
                JsonNode documentNode = nodesNode.path("nodes").path(nodeId).path("document");
                if (documentNode.isMissingNode() || documentNode.isNull()) {
                    throw new BusinessException(404, "未找到指定的 Figma 节点，请检查 node-id 是否正确，或确认当前 Token 对该文件有读取权限");
                }
                response.setNodeId(nodeId);
                response.setNodeName(documentNode.path("name").asText(null));
                response.setNodeType(documentNode.path("type").asText(null));
                response.setExternalUrl("https://www.figma.com/design/" + fileKey + "/context?node-id=" + nodeId.replace(":", "-"));
            }
            response.setMetadataJson(objectMapper.writeValueAsString(fileNode));
            IntegrationDiagnostics.recordSuccess("figma", "read_context", elapsedMs(startedAt));
            return response;
        } catch (BusinessException ex) {
            IntegrationDiagnostics.recordFailure(
                    "figma",
                    "read_context",
                    classifyFigmaBusinessMessage(ex.getMessage()),
                    ex.getMessage(),
                    requestId,
                    elapsedMs(startedAt)
            );
            throw ex;
        } catch (Exception ex) {
            IntegrationDiagnostics.recordFailure(
                    "figma",
                    "read_context",
                    classifyFigmaException(ex),
                    rootMessage(ex),
                    requestId,
                    elapsedMs(startedAt)
            );
            throw new BusinessException(500, "读取 Figma 上下文失败: " + rootMessage(ex));
        }
    }

    private JsonNode get(FigmaConfig config, String path) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.timeoutMs()))
                .build();
        String requestId = OutboundRequestSupport.currentRequestIdOrGenerate();
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.apiBaseUrl() + path))
                    .timeout(Duration.ofMillis(config.timeoutMs()))
                    .header("X-Figma-Token", config.apiKey())
                    .header(OutboundRequestSupport.REQUEST_ID_HEADER, requestId)
                    .GET()
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    if (attempt < MAX_ATTEMPTS && OutboundRequestSupport.isRetryableStatus(response.statusCode())) {
                        log.warn("Figma transient HTTP error, retrying. requestId={}, status={}, attempt={}/{}",
                                requestId, response.statusCode(), attempt, MAX_ATTEMPTS);
                        OutboundRequestSupport.backoff(attempt);
                        continue;
                    }
                    throw new BusinessException(500, mapFigmaHttpError(response.statusCode(), response.body()));
                }
                return objectMapper.readTree(response.body());
            } catch (BusinessException ex) {
                throw ex;
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (attempt < MAX_ATTEMPTS && OutboundRequestSupport.isRetryableException(ex)) {
                    log.warn("Figma transient call failure, retrying. requestId={}, reason={}, attempt={}/{}",
                            requestId, ex.getClass().getSimpleName(), attempt, MAX_ATTEMPTS);
                    OutboundRequestSupport.backoff(attempt);
                    continue;
                }
                throw ex;
            }
        }
        throw new IllegalStateException("Figma call failed without response");
    }

    private FigmaConfig resolveConfig() {
        if (!Boolean.TRUE.equals(systemConfigService.getBoolean(CONFIG_ENABLED, false))) {
            throw new BusinessException(400, "Figma 集成未启用，请在系统配置的工具集成分组打开 integration.figma.enabled");
        }
        String apiBaseUrl = trim(systemConfigService.getString(CONFIG_API_BASE_URL, "https://api.figma.com/v1"));
        String apiKey = trim(systemConfigService.getString(CONFIG_API_KEY, ""));
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            throw new BusinessException(400, "Figma API 地址未配置，请检查 integration.figma.apiBaseUrl");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(400, "Figma API Key 未配置，请检查 integration.figma.apiKey");
        }
        return new FigmaConfig(
                apiBaseUrl.replaceAll("/$", ""),
                apiKey,
                safeTimeout(systemConfigService.getInt(CONFIG_TIMEOUT_MS, DEFAULT_TIMEOUT_MS))
        );
    }

    private String normalizeNodeId(String value) {
        String next = trim(value);
        if (next == null || next.isBlank()) {
            return null;
        }
        return next.replace("-", ":");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getClass().getSimpleName() : current.getMessage();
    }

    private String mapFigmaHttpError(int statusCode, String responseBody) {
        if (statusCode == 401 || statusCode == 403) {
            return "Figma 鉴权失败，请检查 integration.figma.apiKey 是否有效，并确认该 Token 有目标文件的读取权限";
        }
        if (statusCode == 404) {
            return "未找到目标 Figma 文件或节点，请检查链接、File Key 和 Node ID 是否正确";
        }
        if (OutboundRequestSupport.isRetryableStatus(statusCode) || statusCode >= 500) {
            return "Figma 服务当前不可用，请稍后重试并检查目标文件是否可访问";
        }
        return "Figma 响应异常: HTTP " + statusCode + firstResponseMessageSuffix(responseBody);
    }

    private int safeTimeout(Integer timeoutMs) {
        return timeoutMs == null || timeoutMs <= 0 ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }

    private String classifyFigmaBusinessMessage(String message) {
        String lower = message == null ? "" : message.toLowerCase();
        if (lower.contains("鉴权")) {
            return "auth";
        }
        if (lower.contains("未找到目标") || lower.contains("未找到指定的")) {
            return "target_not_found";
        }
        if (lower.contains("file key 未提供") || lower.contains("链接格式")) {
            return "payload_invalid";
        }
        if (lower.contains("不可用")) {
            return "upstream_unavailable";
        }
        return "business_error";
    }

    private String classifyFigmaException(Exception ex) {
        if (OutboundRequestSupport.isRetryableException(ex)) {
            return "network";
        }
        return "exception";
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    private String firstResponseMessageSuffix(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("message").asText(null);
            if (message == null || message.isBlank()) {
                message = root.path("err").asText(null);
            }
            return message == null || message.isBlank() ? "" : " - " + message;
        } catch (Exception _error) {
            return "";
        }
    }

    record FigmaConfig(String apiBaseUrl, String apiKey, int timeoutMs) {
    }
}
