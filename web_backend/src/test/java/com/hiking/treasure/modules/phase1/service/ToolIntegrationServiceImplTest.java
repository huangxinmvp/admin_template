package com.hiking.treasure.modules.phase1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiking.treasure.modules.phase1.domain.dto.command.FigmaContextPreviewDTO;
import com.hiking.treasure.modules.phase1.integration.FigmaIntegrationClient;
import com.hiking.treasure.modules.phase1.integration.LinearIntegrationClient;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadRequest;
import com.hiking.treasure.modules.phase1.integration.ToolIntegrationPayloads.FigmaContextReadResponse;
import com.hiking.treasure.modules.phase1.service.impl.ToolIntegrationServiceImpl;
import com.hiking.treasure.modules.phase1.entity.Project;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolIntegrationServiceImplTest {

    @Test
    void previewFigmaContextParsesFullDesignUrlIntoFileKeyAndNodeId() {
        ProjectService projectService = mock(ProjectService.class);
        ClarificationItemService clarificationItemService = mock(ClarificationItemService.class);
        DecisionItemService decisionItemService = mock(DecisionItemService.class);
        ProjectToolBindingService projectToolBindingService = mock(ProjectToolBindingService.class);
        ToolIntegrationAuditService toolIntegrationAuditService = mock(ToolIntegrationAuditService.class);
        LinearIntegrationClient linearIntegrationClient = mock(LinearIntegrationClient.class);
        FigmaIntegrationClient figmaIntegrationClient = mock(FigmaIntegrationClient.class);

        Project project = new Project();
        project.setId("project-1");
        when(projectService.getById("project-1")).thenReturn(project);

        AtomicReference<FigmaContextReadRequest> requestRef = new AtomicReference<>();
        when(figmaIntegrationClient.readContext(any())).thenAnswer(invocation -> {
            FigmaContextReadRequest request = invocation.getArgument(0);
            requestRef.set(request);
            FigmaContextReadResponse response = new FigmaContextReadResponse();
            response.setFileKey(request.getFileKey());
            response.setFileName("AICoOS");
            response.setNodeId(request.getNodeId());
            response.setNodeName("Page 1");
            response.setExternalUrl("https://www.figma.com/design/" + request.getFileKey() + "/context?node-id=0-1");
            return response;
        });

        ToolIntegrationServiceImpl service = new ToolIntegrationServiceImpl(
                projectService,
                clarificationItemService,
                decisionItemService,
                projectToolBindingService,
                toolIntegrationAuditService,
                null,
                null,
                linearIntegrationClient,
                figmaIntegrationClient,
                new ObjectMapper()
        );

        FigmaContextPreviewDTO dto = new FigmaContextPreviewDTO();
        dto.setFigmaUrl("https://www.figma.com/design/YDc6neOeUqoFQRIIHDyt2t/AICoOS?node-id=0-1&m=dev&t=s6Mf4kwbN038QlUF-1");

        var preview = service.previewFigmaContext("project-1", dto);

        assertNotNull(requestRef.get());
        assertEquals("YDc6neOeUqoFQRIIHDyt2t", requestRef.get().getFileKey());
        assertEquals("0:1", requestRef.get().getNodeId());
        assertEquals("YDc6neOeUqoFQRIIHDyt2t", preview.getFileKey());
        assertEquals("0:1", preview.getNodeId());
        assertEquals("Page 1", preview.getNodeName());
    }
}
