package com.boxoffice.controller;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.service.ResponsibilityCentreService;
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
class ResponsibilityCentreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResponsibilityCentreService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin")
    void testGetAll() throws Exception {
        ResponsibilityCentreDTO dto = ResponsibilityCentreDTO.builder().id(1L).name("Test RC").build();
        when(service.getAllForUser("admin")).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/responsibility-centres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test RC"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testCreate() throws Exception {
        ResponsibilityCentreDTO dto = ResponsibilityCentreDTO.builder().id(1L).name("New RC").build();
        Map<String, String> request = new HashMap<>();
        request.put("name", "New RC");
        request.put("description", "Desc");

        when(service.create(eq("New RC"), eq("Desc"), eq("admin"))).thenReturn(dto);

        mockMvc.perform(post("/api/responsibility-centres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New RC"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testUpdate() throws Exception {
        ResponsibilityCentreDTO dto = ResponsibilityCentreDTO.builder().id(1L).name("Updated RC").build();
        Map<String, String> request = new HashMap<>();
        request.put("name", "Updated RC");
        request.put("description", "New Desc");

        when(service.update(eq(1L), eq("Updated RC"), eq("New Desc"), eq("admin"))).thenReturn(dto);

        mockMvc.perform(put("/api/responsibility-centres/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated RC"));
    }

    @Test
    @WithMockUser(username = "admin")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/responsibility-centres/1"))
                .andExpect(status().isNoContent());
    }
}
