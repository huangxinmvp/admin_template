package com.hiking.treasure.modules.phase1.integration;

import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearCommentCreateResponse;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.LinearIssueCreateResponse;

public interface LinearIntegrationClient {

    LinearIssueCreateResponse createIssue(LinearIssueCreateRequest request);

    LinearCommentCreateResponse createComment(LinearCommentCreateRequest request);
}
