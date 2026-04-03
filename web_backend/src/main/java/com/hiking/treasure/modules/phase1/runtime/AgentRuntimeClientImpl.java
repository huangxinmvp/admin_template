package com.hiking.treasure.modules.phase1.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.diagnostics.IntegrationDiagnostics;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.util.OutboundRequestSupport;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.BudgetImpactSuggestionRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.BudgetImpactSuggestionResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.ClarificationGenerateRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.ClarificationGenerateResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.DecisionBudgetReviewRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.DecisionBudgetReviewResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.DecisionPromotionSuggestionRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.DecisionPromotionSuggestionResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.MeetingSummaryRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.MeetingSummaryResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.ProductArchitectureBriefRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.ProductArchitectureBriefResponse;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.RequirementClarificationReviewRequest;
import com.hiking.treasure.modules.phase1.runtime.AgentRuntimePayloads.RequirementClarificationReviewResponse;
import com.hiking.treasure.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentRuntimeClientImpl implements AgentRuntimeClient {

    static final String CONFIG_ENABLED = "agentRuntime.enabled";
    static final String CONFIG_BASE_URL = "agentRuntime.baseUrl";
    static final String CONFIG_API_KEY = "agentRuntime.apiKey";
    static final String CONFIG_TIMEOUT_MS = "agentRuntime.timeoutMs";
    static final String CONFIG_PROVIDER = "agentRuntime.provider";
    static final String CONFIG_MODEL = "agentRuntime.model";
    static final String CONFIG_MOCK_MODE = "agentRuntime.mockMode";
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int MAX_ATTEMPTS = 2;

    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;

    @Override
    public ClarificationGenerateResponse generateClarificationSuggestions(ClarificationGenerateRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/clarifications/generate", request, ClarificationGenerateResponse.class);
    }

    @Override
    public DecisionPromotionSuggestionResponse generateDecisionPromotionSuggestions(
            DecisionPromotionSuggestionRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/decisions/promotion-suggestions", request, DecisionPromotionSuggestionResponse.class);
    }

    @Override
    public BudgetImpactSuggestionResponse generateBudgetImpactSuggestion(BudgetImpactSuggestionRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/budget/impact-suggestions", request, BudgetImpactSuggestionResponse.class);
    }

    @Override
    public RequirementClarificationReviewResponse generateRequirementClarificationReview(
            RequirementClarificationReviewRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/collaboration/requirement-clarification-review",
                request,
                RequirementClarificationReviewResponse.class);
    }

    @Override
    public DecisionBudgetReviewResponse generateDecisionBudgetReview(DecisionBudgetReviewRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/collaboration/decision-budget-review", request, DecisionBudgetReviewResponse.class);
    }

    @Override
    public ProductArchitectureBriefResponse generateProductArchitectureBrief(ProductArchitectureBriefRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/collaboration/product-architecture-brief",
                request,
                ProductArchitectureBriefResponse.class);
    }

    @Override
    public MeetingSummaryResponse generateMeetingSummary(MeetingSummaryRequest request) {
        applyMetaDefaults(request.getMeta());
        return post("/api/v1/meetings/summarize", request, MeetingSummaryResponse.class);
    }

    private void applyMetaDefaults(AgentRuntimePayloads.RequestMeta meta) {
        if (meta == null) {
            return;
        }
        if (meta.getProviderHint() == null || meta.getProviderHint().isBlank()) {
            if (Boolean.TRUE.equals(systemConfigService.getBoolean(CONFIG_MOCK_MODE, true))) {
                meta.setProviderHint("mock");
            } else {
                meta.setProviderHint(systemConfigService.getString(CONFIG_PROVIDER, "mock"));
            }
        }
        if (meta.getModelHint() == null || meta.getModelHint().isBlank()) {
            meta.setModelHint(systemConfigService.getString(CONFIG_MODEL, "mock-suggestion-v1"));
        }
    }

    private <T> T post(String path, Object payload, Class<T> responseType) {
        RuntimeConfig config = resolveConfig();
        try {
            String responseBody = send(config, path, payload);
            return objectMapper.readValue(responseBody, responseType);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            if (OutboundRequestSupport.isRetryableException(ex)) {
                throw new BusinessException(500, "Agent Runtime 超时或网络不可达，请检查运行时地址、端口和健康状态");
            }
            throw new BusinessException(500, "调用 Agent Runtime 失败: " + rootMessage(ex));
        }
    }

    private String send(RuntimeConfig config, String path, Object payload) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.timeoutMs()))
                .build();
        String requestId = OutboundRequestSupport.currentRequestIdOrGenerate();
        String payloadJson = objectMapper.writeValueAsString(payload);
        long startedAt = System.nanoTime();
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.baseUrl() + path))
                    .timeout(Duration.ofMillis(config.timeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("X-Agent-Runtime-Key", config.apiKey())
                    .header(OutboundRequestSupport.REQUEST_ID_HEADER, requestId)
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    IntegrationDiagnostics.recordSuccess("agent_runtime", path, elapsedMs(startedAt));
                    return response.body();
                }
                BusinessException statusException =
                        new BusinessException(500, mapRuntimeHttpError(response.statusCode(), path, response.body()));
                if (attempt < MAX_ATTEMPTS && OutboundRequestSupport.isRetryableStatus(response.statusCode())) {
                    log.warn("Agent Runtime transient HTTP error, retrying. requestId={}, path={}, status={}, attempt={}/{}",
                            requestId, path, response.statusCode(), attempt, MAX_ATTEMPTS);
                    OutboundRequestSupport.backoff(attempt);
                    continue;
                }
                IntegrationDiagnostics.recordFailure(
                        "agent_runtime",
                        path,
                        classifyRuntimeStatus(response.statusCode()),
                        statusException.getMessage(),
                        requestId,
                        elapsedMs(startedAt)
                );
                throw statusException;
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                lastException = ex;
                if (attempt < MAX_ATTEMPTS && OutboundRequestSupport.isRetryableException(ex)) {
                    log.warn("Agent Runtime transient call failure, retrying. requestId={}, path={}, reason={}, attempt={}/{}",
                            requestId, path, ex.getClass().getSimpleName(), attempt, MAX_ATTEMPTS);
                    OutboundRequestSupport.backoff(attempt);
                    continue;
                }
                IntegrationDiagnostics.recordFailure(
                        "agent_runtime",
                        path,
                        classifyRuntimeException(ex),
                        rootMessage(ex),
                        requestId,
                        elapsedMs(startedAt)
                );
                throw ex;
            }
        }
        throw lastException == null ? new IllegalStateException("Agent Runtime call failed without exception")
                : lastException;
    }

    private RuntimeConfig resolveConfig() {
        if (!Boolean.TRUE.equals(systemConfigService.getBoolean(CONFIG_ENABLED, true))) {
            throw new BusinessException(400, "Agent Runtime 未启用");
        }
        String baseUrl = trim(systemConfigService.getString(CONFIG_BASE_URL, "http://127.0.0.1:8091"));
        String apiKey = systemConfigService.getString(CONFIG_API_KEY, "agent-runtime-local-key");
        Integer timeoutMs = systemConfigService.getInt(CONFIG_TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(400, "Agent Runtime 地址未配置");
        }
        return new RuntimeConfig(baseUrl.replaceAll("/$", ""), apiKey, timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs);
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

    private String mapRuntimeHttpError(int statusCode, String path, String responseBody) {
        if (statusCode == 401 || statusCode == 403) {
            return "Agent Runtime 鉴权失败，请检查 agentRuntime.apiKey 与运行时共享密钥是否一致";
        }
        if (statusCode == 404) {
            return "Agent Runtime 接口不存在，请检查运行时版本或 baseUrl/path 是否正确";
        }
        if (statusCode == 400 || statusCode == 422) {
            return "Agent Runtime 请求或载荷无效，请检查请求结构、必填字段和当前运行时版本";
        }
        if (OutboundRequestSupport.isRetryableStatus(statusCode) || statusCode >= 500) {
            return "Agent Runtime 当前不可用，请稍后重试并检查运行时健康状态";
        }
        return "Agent Runtime 响应异常: HTTP " + statusCode + " (" + path + ")" + extractResponseHint(responseBody);
    }

    private String classifyRuntimeStatus(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return "auth";
        }
        if (statusCode == 404) {
            return "target_not_found";
        }
        if (statusCode == 400 || statusCode == 422) {
            return "payload_invalid";
        }
        if (OutboundRequestSupport.isRetryableStatus(statusCode) || statusCode >= 500) {
            return "upstream_unavailable";
        }
        return "unknown_http";
    }

    private String classifyRuntimeException(Exception ex) {
        if (OutboundRequestSupport.isRetryableException(ex)) {
            return "network";
        }
        return "exception";
    }

    private String extractResponseHint(String responseBody) {
        try {
            var root = objectMapper.readTree(responseBody);
            String detail = root.path("detail").asText(null);
            if (detail == null || detail.isBlank()) {
                detail = root.path("message").asText(null);
            }
            return detail == null || detail.isBlank() ? "" : " - " + detail;
        } catch (Exception _error) {
            return "";
        }
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    record RuntimeConfig(String baseUrl, String apiKey, int timeoutMs) {
    }
}
