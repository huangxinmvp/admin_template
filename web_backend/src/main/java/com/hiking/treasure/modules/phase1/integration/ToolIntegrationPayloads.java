package com.hiking.treasure.modules.phase1.integration;

import lombok.Data;

public final class ToolIntegrationPayloads {

    private ToolIntegrationPayloads() {
    }

    @Data
    public static class LinearIssueCreateRequest {
        private String teamId;
        private String title;
        private String description;
    }

    @Data
    public static class LinearIssueCreateResponse {
        private String id;
        private String identifier;
        private String title;
        private String url;
    }

    @Data
    public static class LinearCommentCreateRequest {
        private String issueId;
        private String body;
    }

    @Data
    public static class LinearCommentCreateResponse {
        private String id;
        private String url;
        private String body;
    }

    @Data
    public static class FigmaContextReadRequest {
        private String fileKey;
        private String nodeId;
    }

    @Data
    public static class FigmaContextReadResponse {
        private String fileKey;
        private String fileName;
        private String nodeId;
        private String nodeName;
        private String nodeType;
        private String lastModifiedAt;
        private String externalUrl;
        private String metadataJson;
    }
}
