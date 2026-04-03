package com.hiking.treasure.modules.phase1.integration;

import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadResponse;

public interface FigmaIntegrationClient {

    FigmaContextReadResponse readContext(FigmaContextReadRequest request);
}
