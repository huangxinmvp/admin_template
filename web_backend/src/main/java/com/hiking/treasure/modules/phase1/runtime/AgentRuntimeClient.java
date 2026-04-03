package com.hiking.treasure.modules.phase1.runtime;

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

public interface AgentRuntimeClient {

    ClarificationGenerateResponse generateClarificationSuggestions(ClarificationGenerateRequest request);

    DecisionPromotionSuggestionResponse generateDecisionPromotionSuggestions(
            DecisionPromotionSuggestionRequest request);

    BudgetImpactSuggestionResponse generateBudgetImpactSuggestion(BudgetImpactSuggestionRequest request);

    RequirementClarificationReviewResponse generateRequirementClarificationReview(
            RequirementClarificationReviewRequest request);

    DecisionBudgetReviewResponse generateDecisionBudgetReview(DecisionBudgetReviewRequest request);

    ProductArchitectureBriefResponse generateProductArchitectureBrief(ProductArchitectureBriefRequest request);

    MeetingSummaryResponse generateMeetingSummary(MeetingSummaryRequest request);
}
