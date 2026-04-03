package com.hiking.treasure.controller;

import com.hiking.treasure.domain.vo.system.SystemHealthVO;
import com.hiking.treasure.service.SystemHealthService;
import com.hiking.treasure.service.SystemPortalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SystemPortalHealthMvcTest {

    @Mock
    private SystemPortalService systemPortalService;
    @Mock
    private SystemHealthService systemHealthService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SystemPortalController controller = new SystemPortalController(systemPortalService, systemHealthService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void healthReturnsOkWhenDatabaseIsReady() throws Exception {
        SystemHealthVO health = new SystemHealthVO();
        health.setStatus("ok");
        health.setApplication("admin-template");
        health.setDatabaseReady(true);
        health.setTimestamp(1711900800000L);
        when(systemHealthService.getHealth()).thenReturn(health);

        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.application").value("admin-template"))
                .andExpect(jsonPath("$.databaseReady").value(true))
                .andExpect(jsonPath("$.timestamp").value(1711900800000L));
    }

    @Test
    void healthReturnsServiceUnavailableWhenDatabaseIsNotReady() throws Exception {
        SystemHealthVO health = new SystemHealthVO();
        health.setStatus("degraded");
        health.setApplication("admin-template");
        health.setDatabaseReady(false);
        health.setTimestamp(1711900800000L);
        when(systemHealthService.getHealth()).thenReturn(health);

        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("degraded"))
                .andExpect(jsonPath("$.databaseReady").value(false));
    }
}
