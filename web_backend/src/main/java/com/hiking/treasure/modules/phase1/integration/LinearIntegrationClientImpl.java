package com.hiking.treasure.modules.phase1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.common.diagnostics.IntegrationDiagnostics;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.util.OutboundRequestSupport;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateResponse;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateResponse;
import com.hiking.treasure.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinearIntegrationClientImpl implements LinearIntegrationClient {

    static final String CONFIG_ENABLED = "integration.linear.enabled";
    static final String CONFIG_GRAPHQL_URL = "integration.linear.graphqlUrl";
    static final String CONFIG_API_KEY = "integration.linear.apiKey";
    static final String CONFIG_DEFAULT_TEAM_ID = "integration.linear.defaultTeamId";
    static final String CONFIG_TIMEOUT_MS = "integration.linear.timeoutMs";
    static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int MAX_ATTEMPTS = 2;

    private final SystemConfigService systemConfigService;
    private final ObjectMapper objectMapper;

    @Override
    public LinearIssueCreateResponse createIssue(LinearIssueCreateRequest request) {
        LinearConfig config = resolveConfig();
        String teamId = textOrDefault(request.getTeamId(), config.defaultTeamId());
        if (teamId == null || teamId.isBlank()) {
            throw new BusinessException(400, "Linear Team ID 未配置，请在系统配置填写 integration.linear.defaultTeamId，或在当前弹窗里显式输入 Team ID");
        }
        JsonNode data = executeMutation(
                config,
                "issue_create",
                """
                        mutation CreateIssue($input: IssueCreateInput!) {
                          issueCreate(input: $input) {
                            success
                            issue {
                              id
                              identifier
                              title
                              url
                            }
                          }
                        }
                        """,
                Map.of(
                        "input",
                        Map.of(
                                "teamId", teamId,
                                "title", textOrDefault(request.getTitle(), "AICoOS External Sync"),
                                "description", textOrDefault(request.getDescription(), "")
                        )
                )
        );
        JsonNode issueNode = data.path("issueCreate").path("issue");
        if (issueNode.isMissingNode() || issueNode.isNull()) {
            throw new BusinessException(500, "Linear 未返回创建后的 Issue");
        }
        LinearIssueCreateResponse response = new LinearIssueCreateResponse();
        response.setId(issueNode.path("id").asText(null));
        response.setIdentifier(issueNode.path("identifier").asText(null));
        response.setTitle(issueNode.path("title").asText(null));
        response.setUrl(issueNode.path("url").asText(null));
        return response;
    }

    @Override
    public LinearCommentCreateResponse createComment(LinearCommentCreateRequest request) {
        LinearConfig config = resolveConfig();
        if (request.getIssueId() == null || request.getIssueId().isBlank()) {
            throw new BusinessException(400, "Linear Comment 需要目标 Issue ID；请先为项目建立 Linear 主工作项映射，或切换为 Issue 模式");
        }
        JsonNode data = executeMutation(
                config,
                "comment_create",
                """
                        mutation CreateComment($input: CommentCreateInput!) {
                          commentCreate(input: $input) {
                            success
                            comment {
                              id
                              body
                              url
                            }
                          }
                        }
                        """,
                Map.of(
                        "input",
                        Map.of(
                                "issueId", request.getIssueId(),
                                "body", textOrDefault(request.getBody(), "")
                        )
                )
        );
        JsonNode commentNode = data.path("commentCreate").path("comment");
        if (commentNode.isMissingNode() || commentNode.isNull()) {
            throw new BusinessException(500, "Linear 未返回创建后的 Comment");
        }
        LinearCommentCreateResponse response = new LinearCommentCreateResponse();
        response.setId(commentNode.path("id").asText(null));
        response.setBody(commentNode.path("body").asText(null));
        response.setUrl(commentNode.path("url").asText(null));
        return response;
    }

    private JsonNode executeMutation(LinearConfig config, String operation, String query, Map<String, Object> variables) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(config.timeoutMs()))
                    .build();
            String requestId = OutboundRequestSupport.currentRequestIdOrGenerate();
            long startedAt = System.nanoTime();
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "query", query,
                    "variables", variables
            ));
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(config.graphqlUrl()))
                        .timeout(Duration.ofMillis(config.timeoutMs()))
                        .header("Content-Type", "application/json")
                        .header("Authorization", config.apiKey())
                        .header(OutboundRequestSupport.REQUEST_ID_HEADER, requestId)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                try {
                    HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        log.warn("Linear returned non-2xx response. requestId={}, status={}, body={}",
                                requestId, response.statusCode(), response.body());
                        if (attempt < MAX_ATTEMPTS && OutboundRequestSupport.isRetryableStatus(response.statusCode())) {
                            log.warn("Linear transient HTTP error, retrying. requestId={}, status={}, attempt={}/{}",
                                    requestId, response.statusCode(), attempt, MAX_ATTEMPTS);
                            OutboundRequestSupport.backoff(attempt);
                            continue;
                        }
                        IntegrationDiagnostics.recordFailure(
                                "linear",
                                operation,
                                classifyLinearHttpFailure(response.statusCode(), response.body()),
                                mapLinearHttpError(response.statusCode(), response.body()),
                                requestId,
                                elapsedMs(startedAt)
                        );
                        throw new BusinessException(500, mapLinearHttpError(response.statusCode(), response.body()));
                    }
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode errors = root.path("errors");
                    if (errors.isArray() && !errors.isEmpty()) {
                        String message = errors.get(0).path("message").asText("unknown");
                        IntegrationDiagnostics.recordFailure(
                                "linear",
                                operation,
                                classifyLinearGraphqlMessage(message),
                                mapLinearGraphqlError(message),
                                requestId,
                                elapsedMs(startedAt)
                        );
                        throw new BusinessException(500, mapLinearGraphqlError(message));
                    }
                    IntegrationDiagnostics.recordSuccess("linear", operation, elapsedMs(startedAt));
                    return root.path("data");
                } catch (BusinessException ex) {
                    throw ex;
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    if (attempt < MAX_ATTEMPTS && OutboundRequestSupport.isRetryableException(ex)) {
                        log.warn("Linear transient call failure, retrying. requestId={}, reason={}, attempt={}/{}",
                                requestId, ex.getClass().getSimpleName(), attempt, MAX_ATTEMPTS);
                        OutboundRequestSupport.backoff(attempt);
                        continue;
                    }
                    IntegrationDiagnostics.recordFailure(
                            "linear",
                            operation,
                            classifyLinearException(ex),
                            rootMessage(ex),
                            requestId,
                            elapsedMs(startedAt)
                    );
                    throw ex;
                }
            }
            throw new IllegalStateException("Linear call failed without response");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(500, mapLinearClientError(ex));
        }
    }

    private LinearConfig resolveConfig() {
        if (!Boolean.TRUE.equals(systemConfigService.getBoolean(CONFIG_ENABLED, false))) {
            throw new BusinessException(400, "Linear 集成未启用，请在系统配置的工具集成分组打开 integration.linear.enabled");
        }
        String graphqlUrl = trim(systemConfigService.getString(CONFIG_GRAPHQL_URL, "https://api.linear.app/graphql"));
        String apiKey = trim(systemConfigService.getString(CONFIG_API_KEY, ""));
        if (graphqlUrl == null || graphqlUrl.isBlank()) {
            throw new BusinessException(400, "Linear GraphQL 地址未配置，请检查 integration.linear.graphqlUrl");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(400, "Linear API Key 未配置，请检查 integration.linear.apiKey");
        }
        return new LinearConfig(
                graphqlUrl,
                apiKey,
                trim(systemConfigService.getString(CONFIG_DEFAULT_TEAM_ID, "")),
                safeTimeout(systemConfigService.getInt(CONFIG_TIMEOUT_MS, DEFAULT_TIMEOUT_MS))
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String textOrDefault(String value, String defaultValue) {
        String next = trim(value);
        return next == null || next.isBlank() ? defaultValue : next;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getClass().getSimpleName() : current.getMessage();
    }

    private String mapLinearHttpError(int statusCode, String responseBody) {
        if (statusCode == 401 || statusCode == 403) {
            return "Linear 鉴权失败，请检查 integration.linear.apiKey 是否有效且具备目标 workspace 的访问权限";
        }
        if (statusCode == 404) {
            return "Linear 接口地址不可用，请检查 integration.linear.graphqlUrl 是否正确";
        }
        String graphqlMessage = extractLinearResponseMessage(responseBody);
        String lower = graphqlMessage == null ? "" : graphqlMessage.toLowerCase();
        if (statusCode == 400 && lower.contains("api key as a bearer token")) {
            return "Linear API Key 鉴权头格式错误：当前 GraphQL API 需要直接使用 API Key 作为 Authorization 值，不要添加 Bearer 前缀";
        }
        if (statusCode == 400 && lower.contains("team")) {
            return "Linear Team 无法识别，请检查 integration.linear.defaultTeamId 或当前弹窗中的 Team ID";
        }
        if (statusCode == 400 && lower.contains("issue")) {
            return "Linear 目标 Issue 无法识别，请先确认项目主工作项映射存在且 externalId 是真实 Linear issue id";
        }
        return "Linear 响应异常: HTTP " + statusCode + firstResponseMessageSuffix(responseBody);
    }

    private String mapLinearGraphqlError(String message) {
        String next = message == null ? "" : message;
        String lower = next.toLowerCase();
        if (lower.contains("auth") || lower.contains("permission") || lower.contains("forbidden")) {
            return "Linear 返回权限错误，请检查 API Key 权限或目标 workspace/team 的访问范围";
        }
        if (lower.contains("team")) {
            return "Linear Team 无法识别，请检查 integration.linear.defaultTeamId 或当前弹窗中的 Team ID";
        }
        if (lower.contains("issue")) {
            return "Linear 目标 Issue 无法识别，请先确认项目主工作项映射存在，或改用 Issue 模式";
        }
        if (lower.contains("input") || lower.contains("invalid")) {
            return "Linear 请求参数无效，请检查当前 payload、Team ID、Issue ID 和字段格式";
        }
        return "Linear 错误: " + next;
    }

    private String mapLinearClientError(Exception ex) {
        if (OutboundRequestSupport.isRetryableException(ex)) {
            return "调用 Linear 超时或网络不可达，请检查网络、GraphQL 地址和 Linear 服务可用性";
        }
        return "调用 Linear 失败: " + rootMessage(ex);
    }

    private String firstResponseMessageSuffix(String responseBody) {
        String message = extractLinearResponseMessage(responseBody);
        return message == null || message.isBlank() ? "" : " - " + message;
    }

    private String extractLinearResponseMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("errors").isArray() && !root.path("errors").isEmpty()
                    ? root.path("errors").get(0).path("message").asText(null)
                    : null;
            if (message == null || message.isBlank()) {
                message = root.path("message").asText(null);
            }
            if (message == null || message.isBlank()) {
                message = root.path("error").asText(null);
            }
            return message;
        } catch (Exception _error) {
            return null;
        }
    }

    private int safeTimeout(Integer timeoutMs) {
        return timeoutMs == null || timeoutMs <= 0 ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }

    private String classifyLinearHttpFailure(int statusCode, String responseBody) {
        if (statusCode == 401 || statusCode == 403) {
            return "auth";
        }
        if (statusCode == 404) {
            return "target_not_found";
        }
        String message = extractLinearResponseMessage(responseBody);
        String lower = message == null ? "" : message.toLowerCase();
        if (statusCode == 400 && lower.contains("team")) {
            return "permission_or_team";
        }
        if (statusCode == 400 && lower.contains("issue")) {
            return "target_not_found";
        }
        if (statusCode == 400) {
            return "payload_invalid";
        }
        if (OutboundRequestSupport.isRetryableStatus(statusCode) || statusCode >= 500) {
            return "upstream_unavailable";
        }
        return "unknown_http";
    }

    private String classifyLinearGraphqlMessage(String message) {
        String lower = message == null ? "" : message.toLowerCase();
        if (lower.contains("auth") || lower.contains("permission") || lower.contains("forbidden")) {
            return "auth";
        }
        if (lower.contains("team")) {
            return "permission_or_team";
        }
        if (lower.contains("issue")) {
            return "target_not_found";
        }
        if (lower.contains("input") || lower.contains("invalid")) {
            return "payload_invalid";
        }
        return "graphql_error";
    }

    private String classifyLinearException(Exception ex) {
        if (OutboundRequestSupport.isRetryableException(ex)) {
            return "network";
        }
        return "exception";
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    record LinearConfig(String graphqlUrl, String apiKey, String defaultTeamId, int timeoutMs) {
    }
}
