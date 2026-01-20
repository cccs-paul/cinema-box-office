package com.boxoffice.controller;

import com.boxoffice.dto.FiscalYearDTO;
import com.boxoffice.dto.FiscalYearRequest;
import com.boxoffice.service.FiscalYearService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FiscalYearControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FiscalYearService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin")
    void testGetByRc() throws Exception {
        FiscalYearDTO dto = FiscalYearDTO.builder().id(10L).name("2024").build();
        when(service.getAllForRc(1L)).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/fiscal-years/rc/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("2024"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testCreate() throws Exception {
        FiscalYearDTO dto = FiscalYearDTO.builder().id(10L).name("2024").build();
        FiscalYearRequest request = FiscalYearRequest.builder().name("2024").rcId(1L).build();

        when(service.create(eq("2024"), eq(1L), eq("admin"))).thenReturn(dto);

        mockMvc.perform(post("/api/fiscal-years")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("2024"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testUpdate() throws Exception {
        FiscalYearDTO dto = FiscalYearDTO.builder().id(10L).name("2024-Updated").build();
        Map<String, String> request = new HashMap<>();
        request.put("name", "2024-Updated");

        when(service.update(eq(10L), eq("2024-Updated"), eq("admin"))).thenReturn(dto);

        mockMvc.perform(put("/api/fiscal-years/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("2024-Updated"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/fiscal-years/10"))
                .andExpect(status().isNoContent());
    }
}
