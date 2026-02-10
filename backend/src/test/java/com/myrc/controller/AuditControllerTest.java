/*
 * myRC - Audit Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.AuditEventDTO;
import com.myrc.dto.ErrorResponse;
import com.myrc.service.AuditService;
import com.myrc.service.RCPermissionService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit tests for AuditController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditController Tests")
class AuditControllerTest {

  @Mock
  private AuditService auditService;

  @Mock
  private RCPermissionService rcPermissionService;

  private AuditController controller;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    controller = new AuditController(auditService, rcPermissionService);
    authentication = createAuthentication("testowner");
  }

  private Authentication createAuthentication(String username) {
    return new Authentication() {
      @Override
      public String getName() { return username; }
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
      @Override
      public Object getCredentials() { return null; }
      @Override
      public Object getDetails() { return null; }
      @Override
      public Object getPrincipal() { return username; }
      @Override
      public boolean isAuthenticated() { return true; }
      @Override
      public void setAuthenticated(boolean isAuthenticated) { }
    };
  }

  private AuditEventDTO createTestDTO(Long id, String action) {
    AuditEventDTO dto = new AuditEventDTO();
    // Use reflection to set fields since DTO only has getters via fromEntity pattern
    try {
      var idField = AuditEventDTO.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(dto, id);

      var actionField = AuditEventDTO.class.getDeclaredField("action");
      actionField.setAccessible(true);
      actionField.set(dto, action);

      var entityTypeField = AuditEventDTO.class.getDeclaredField("entityType");
      entityTypeField.setAccessible(true);
      entityTypeField.set(dto, "RESPONSIBILITY_CENTRE");

      var usernameField = AuditEventDTO.class.getDeclaredField("username");
      usernameField.setAccessible(true);
      usernameField.set(dto, "testuser");

      var outcomeField = AuditEventDTO.class.getDeclaredField("outcome");
      outcomeField.setAccessible(true);
      outcomeField.set(dto, "SUCCESS");
    } catch (Exception e) {
      throw new RuntimeException("Failed to set DTO field", e);
    }
    return dto;
  }

  @Nested
  @DisplayName("getAuditEvents Tests")
  class GetAuditEventsTests {

    @Test
    @DisplayName("Should return audit events for owner")
    void testGetAuditEventsSuccess() {
      when(rcPermissionService.isOwner(1L, "testowner")).thenReturn(true);
      AuditEventDTO dto1 = createTestDTO(1L, "CREATE_RC");
      AuditEventDTO dto2 = createTestDTO(2L, "UPDATE_RC");
      when(auditService.getAuditEventsForRC(1L)).thenReturn(List.of(dto1, dto2));

      ResponseEntity<?> response = controller.getAuditEvents(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      @SuppressWarnings("unchecked")
      List<AuditEventDTO> body = (List<AuditEventDTO>) response.getBody();
      assertNotNull(body);
      assertEquals(2, body.size());
    }

    @Test
    @DisplayName("Should return 403 for non-owner")
    void testGetAuditEventsForbidden() {
      when(rcPermissionService.isOwner(1L, "testowner")).thenReturn(false);

      ResponseEntity<?> response = controller.getAuditEvents(1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    @DisplayName("Should return empty list when no events")
    void testGetAuditEventsEmpty() {
      when(rcPermissionService.isOwner(1L, "testowner")).thenReturn(true);
      when(auditService.getAuditEventsForRC(1L)).thenReturn(Collections.emptyList());

      ResponseEntity<?> response = controller.getAuditEvents(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      @SuppressWarnings("unchecked")
      List<AuditEventDTO> body = (List<AuditEventDTO>) response.getBody();
      assertNotNull(body);
      assertTrue(body.isEmpty());
    }

    @Test
    @DisplayName("Should use default-user when authentication is null")
    void testGetAuditEventsNullAuth() {
      when(rcPermissionService.isOwner(1L, "default-user")).thenReturn(false);

      ResponseEntity<?> response = controller.getAuditEvents(1L, null);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      verify(rcPermissionService).isOwner(1L, "default-user");
    }
  }

  @Nested
  @DisplayName("getAuditEventsForFiscalYear Tests")
  class GetAuditEventsForFiscalYearTests {

    @Test
    @DisplayName("Should return audit events for fiscal year")
    void testGetAuditEventsForFiscalYearSuccess() {
      when(rcPermissionService.isOwner(1L, "testowner")).thenReturn(true);
      AuditEventDTO dto = createTestDTO(1L, "CREATE_FUNDING_ITEM");
      when(auditService.getAuditEventsForFiscalYear(1L, 10L)).thenReturn(List.of(dto));

      ResponseEntity<?> response = controller.getAuditEventsForFiscalYear(1L, 10L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      @SuppressWarnings("unchecked")
      List<AuditEventDTO> body = (List<AuditEventDTO>) response.getBody();
      assertNotNull(body);
      assertEquals(1, body.size());
    }

    @Test
    @DisplayName("Should return 403 for non-owner")
    void testGetAuditEventsForFiscalYearForbidden() {
      when(rcPermissionService.isOwner(1L, "testowner")).thenReturn(false);

      ResponseEntity<?> response = controller.getAuditEventsForFiscalYear(1L, 10L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
      assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    @DisplayName("Should use default-user when authentication is null")
    void testGetAuditEventsForFiscalYearNullAuth() {
      when(rcPermissionService.isOwner(1L, "default-user")).thenReturn(true);
      when(auditService.getAuditEventsForFiscalYear(1L, 10L)).thenReturn(Collections.emptyList());

      ResponseEntity<?> response = controller.getAuditEventsForFiscalYear(1L, 10L, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(rcPermissionService).isOwner(1L, "default-user");
    }
  }
}
