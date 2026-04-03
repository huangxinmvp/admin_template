package com.hiking.treasure.modules.phase1.runtime;

import lombok.Data;

import java.util.List;

public final class AgentRuntimePayloads {

    private AgentRuntimePayloads() {
    }

    @Data
    public static class RequestMeta {
        private String providerHint;
        private String modelHint;
    }

    @Data
    public static class ClarificationGenerateRequest {
        private String projectName;
        private String projectType;
        private String businessGoal;
        private String featureSummary;
        private String referenceProducts;
        private String timelineExpectation;
        private String budgetRange;
        private String technicalConstraints;
        private String notes;
        private RequestMeta meta;
    }

    @Data
    public static class ClarificationSuggestion {
        private String title;
        private String question;
        private String category;
        private String severity;
        private String suggestedOptions;
        private Boolean blockerFlag;
        private String reason;
        private String governanceReason;
        private String followUpModule;
        private Boolean escalationRecommended;
    }

    @Data
    public static class ClarificationGenerateResponse {
        private String provider;
        private String model;
        private List<ClarificationSuggestion> suggestions;
    }

    @Data
    public static class ClarificationContext {
        private String clarificationId;
        private String title;
        private String question;
        private String category;
        private String severity;
        private String suggestedOptions;
        private String userResponse;
        private String status;
    }

    @Data
    public static class DecisionPromotionSuggestionRequest {
        private String projectName;
        private String projectType;
        private String currentStageCode;
        private List<ClarificationContext> clarifications;
        private String requirementSummary;
        private RequestMeta meta;
    }

    @Data
    public static class DecisionPromotionSuggestion {
        private String clarificationId;
        private String suggestedTitle;
        private String type;
        private String impactSummary;
        private String suggestedOptions;
        private String recommendedOption;
        private Boolean blockerFlag;
        private String reason;
    }

    @Data
    public static class DecisionPromotionSuggestionResponse {
        private String provider;
        private String model;
        private List<DecisionPromotionSuggestion> suggestions;
    }

    @Data
    public static class BudgetImpactSuggestionRequest {
        private String projectName;
        private String projectType;
        private String currentStageCode;
        private String budgetRange;
        private String decisionTitle;
        private String decisionType;
        private String decisionDescription;
        private String decisionImpactSummary;
        private String requirementSummary;
        private RequestMeta meta;
    }

    @Data
    public static class BudgetImpactSuggestion {
        private String budgetImpactSummary;
        private String projectImpactSummary;
        private String deltaRange;
        private List<String> affectedRoles;
        private String confidence;
    }

    @Data
    public static class BudgetImpactSuggestionResponse {
        private String provider;
        private String model;
        private BudgetImpactSuggestion suggestion;
    }

    @Data
    public static class RequirementClarificationReviewRequest {
        private String projectName;
        private String projectType;
        private String currentStageCode;
        private String businessGoal;
        private String featureSummary;
        private String referenceProducts;
        private String timelineExpectation;
        private String budgetRange;
        private String technicalConstraints;
        private String notes;
        private Integer existingClarificationCount;
        private RequestMeta meta;
    }

    @Data
    public static class RequirementClarificationReviewResponse {
        private String provider;
        private String model;
        private List<String> participants;
        private String scenarioLabel;
        private String collaborationSummary;
        private String recommendedOperatorAction;
        private java.util.Map<String, Object> governanceLinkage;
        private List<String> roleInsights;
        private String selectionGuidance;
        private String governanceInterpretation;
        private Boolean decisionEscalationAdvised;
        private List<String> followUpHints;
        private String blockerAssessment;
        private List<String> nextQuestions;
        private List<ClarificationSuggestion> suggestions;
    }

    @Data
    public static class DecisionBudgetReviewRequest {
        private String projectName;
        private String projectType;
        private String currentStageCode;
        private String budgetRange;
        private String decisionTitle;
        private String decisionType;
        private String decisionDescription;
        private String decisionImpactSummary;
        private String suggestedOptions;
        private String recommendedOption;
        private Boolean blockerFlag;
        private String budgetImpactSummary;
        private String projectImpactSummary;
        private String requirementSummary;
        private RequestMeta meta;
    }

    @Data
    public static class DecisionBudgetReviewSuggestion {
        private String decisionRecommendation;
        private String budgetImpactNote;
        private Boolean budgetConfirmationAdvised;
        private String recommendedOption;
        private String projectImpactNote;
        private String blockerAssessment;
        private List<String> nextSteps;
    }

    @Data
    public static class DecisionBudgetReviewResponse {
        private String provider;
        private String model;
        private List<String> participants;
        private String scenarioLabel;
        private String collaborationSummary;
        private String recommendedOperatorAction;
        private java.util.Map<String, Object> governanceLinkage;
        private java.util.Map<String, String> roleInsights;
        private List<String> riskFlags;
        private String decisionLinkageSummary;
        private String budgetLinkageSummary;
        private List<String> followUpHints;
        private DecisionBudgetReviewSuggestion suggestion;
    }

    @Data
    public static class ProductArchitectureBriefRequest {
        private String projectName;
        private String projectType;
        private String currentStageCode;
        private String businessGoal;
        private String featureSummary;
        private String technicalConstraints;
        private String requirementSummary;
        private String focusNotes;
        private RequestMeta meta;
    }

    @Data
    public static class ProductArchitectureBriefSuggestion {
        private String briefTitle;
        private String solutionBrief;
        private List<String> risks;
        private List<String> openQuestions;
        private List<String> nextSteps;
    }

    @Data
    public static class ProductArchitectureBriefResponse {
        private String provider;
        private String model;
        private List<String> participants;
        private String scenarioLabel;
        private String collaborationSummary;
        private String recommendedOperatorAction;
        private java.util.Map<String, Object> governanceLinkage;
        private java.util.Map<String, String> roleInsights;
        private List<String> architectureFocusAreas;
        private List<String> deliveryImplications;
        private String projectGovernanceLinkageSummary;
        private List<String> followUpHints;
        private ProductArchitectureBriefSuggestion suggestion;
    }

    @Data
    public static class MeetingSummaryRequest {
        private String projectName;
        private String currentStageCode;
        private String meetingTitle;
        private String rawNotes;
        private String sourceType;
        private String sourceObjectId;
        private RequestMeta meta;
    }

    @Data
    public static class MeetingDecisionCandidate {
        private String title;
        private String decisionType;
        private Boolean blockerFlag;
        private String recommendedOption;
    }

    @Data
    public static class MeetingSummarySuggestion {
        private String meetingTitle;
        private String summary;
        private List<String> actionItems;
        private List<String> openQuestions;
        private List<MeetingDecisionCandidate> decisionCandidates;
    }

    @Data
    public static class MeetingSummaryResponse {
        private String provider;
        private String model;
        private MeetingSummarySuggestion suggestion;
    }
}
