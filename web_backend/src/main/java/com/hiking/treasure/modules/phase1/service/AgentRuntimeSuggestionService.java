package com.hiking.treasure.modules.phase1.service;

import com.hiking.treasure.modules.phase1.domain.dto.command.ApplyClarificationSuggestionsDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.ApplyDecisionSuggestionsDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.BudgetImpactSuggestionApplyDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.DecisionBudgetReviewApplyDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.MeetingSummaryGenerateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.command.ProductArchitectureBriefGenerateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.BudgetImpactSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ClarificationSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionBudgetReviewVO;
import com.hiking.treasure.modules.phase1.domain.vo.DecisionPromotionSuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.MeetingSummarySuggestionVO;
import com.hiking.treasure.modules.phase1.domain.vo.ProductArchitectureBriefVO;
import com.hiking.treasure.modules.phase1.domain.vo.RequirementClarificationReviewVO;
import com.hiking.treasure.modules.phase1.entity.ClarificationItem;
import com.hiking.treasure.modules.phase1.entity.DecisionItem;

import java.util.List;

public interface AgentRuntimeSuggestionService {

    List<ClarificationSuggestionVO> generateClarificationSuggestions(String projectId);

    List<ClarificationItem> applyClarificationSuggestions(String projectId, ApplyClarificationSuggestionsDTO dto);

    List<DecisionPromotionSuggestionVO> generateDecisionSuggestions(String projectId);

    List<DecisionItem> applyDecisionSuggestions(String projectId, ApplyDecisionSuggestionsDTO dto);

    BudgetImpactSuggestionVO generateDecisionBudgetImpactSuggestion(String decisionItemId);

    DecisionItem applyDecisionBudgetImpactSuggestion(String decisionItemId, BudgetImpactSuggestionApplyDTO dto);

    RequirementClarificationReviewVO generateRequirementClarificationReview(String projectId);

    DecisionBudgetReviewVO generateDecisionBudgetReview(String decisionItemId);

    DecisionItem applyDecisionBudgetReview(String decisionItemId, DecisionBudgetReviewApplyDTO dto);

    ProductArchitectureBriefVO generateProductArchitectureBrief(
            String projectId,
            ProductArchitectureBriefGenerateDTO dto);

    MeetingSummarySuggestionVO generateMeetingSummary(String projectId, MeetingSummaryGenerateDTO dto);
}
