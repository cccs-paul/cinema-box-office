/*
 * myRC - Procurement Item Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.ProcurementItemDTO;
import com.myrc.dto.ProcurementQuoteDTO;
import com.myrc.service.ProcurementItemService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
 * Unit tests for ProcurementItemController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class ProcurementItemControllerTest {

  @Mock
  private ProcurementItemService procurementItemService;

  private Authentication authentication;
  private ProcurementItemController controller;
  private ProcurementItemDTO testItem;
  private ProcurementQuoteDTO testQuote;

  @BeforeEach
  void setUp() {
    controller = new ProcurementItemController(procurementItemService);
    authentication = createAuthentication("testuser");

    testItem = new ProcurementItemDTO();
    testItem.setId(1L);
    testItem.setName("Test Procurement Item");
    testItem.setDescription("Test Description");
    testItem.setPurchaseRequisition("PR-001");
    testItem.setFiscalYearId(1L);
    testItem.setCurrentStatus("DRAFT");

    testQuote = new ProcurementQuoteDTO();
    testQuote.setId(1L);
    testQuote.setVendorName("Test Vendor");
    testQuote.setAmount(new BigDecimal("1000.00"));
    testQuote.setCurrency("CAD");
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
      public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }
    };
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Nested
  @DisplayName("getProcurementItems Tests")
  class GetProcurementItemsTests {

    @Test
    @DisplayName("Should return procurement items successfully")
    void shouldReturnProcurementItemsSuccessfully() {
      when(procurementItemService.getProcurementItemsByFiscalYearId(1L, "testuser"))
          .thenReturn(Arrays.asList(testItem));

      ResponseEntity<List<ProcurementItemDTO>> response = 
          controller.getProcurementItems(1L, 1L, null, null, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should filter by status when provided")
    void shouldFilterByStatusWhenProvided() {
      when(procurementItemService.getProcurementItemsByFiscalYearIdAndStatus(1L, "DRAFT", "testuser"))
          .thenReturn(Arrays.asList(testItem));

      ResponseEntity<List<ProcurementItemDTO>> response = 
          controller.getProcurementItems(1L, 1L, "DRAFT", null, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(procurementItemService).getProcurementItemsByFiscalYearIdAndStatus(1L, "DRAFT", "testuser");
    }

    @Test
    @DisplayName("Should search when search term provided")
    void shouldSearchWhenSearchTermProvided() {
      when(procurementItemService.searchProcurementItems(1L, "test", "testuser"))
          .thenReturn(Arrays.asList(testItem));

      ResponseEntity<List<ProcurementItemDTO>> response = 
          controller.getProcurementItems(1L, 1L, null, "test", authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(procurementItemService).searchProcurementItems(1L, "test", "testuser");
    }

    @Test
    @DisplayName("Should return forbidden when access denied")
    void shouldReturnForbiddenWhenAccessDenied() {
      when(procurementItemService.getProcurementItemsByFiscalYearId(1L, "testuser"))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<ProcurementItemDTO>> response = 
          controller.getProcurementItems(1L, 1L, null, null, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getProcurementItem Tests")
  class GetProcurementItemTests {

    @Test
    @DisplayName("Should return procurement item when found")
    void shouldReturnProcurementItemWhenFound() {
      when(procurementItemService.getProcurementItemById(1L, "testuser"))
          .thenReturn(Optional.of(testItem));

      ResponseEntity<ProcurementItemDTO> response = 
          controller.getProcurementItem(1L, 1L, 1L, false, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should include quotes when requested")
    void shouldIncludeQuotesWhenRequested() {
      when(procurementItemService.getProcurementItemWithQuotes(1L, "testuser"))
          .thenReturn(Optional.of(testItem));

      ResponseEntity<ProcurementItemDTO> response = 
          controller.getProcurementItem(1L, 1L, 1L, true, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(procurementItemService).getProcurementItemWithQuotes(1L, "testuser");
    }

    @Test
    @DisplayName("Should return not found when item does not exist")
    void shouldReturnNotFoundWhenItemDoesNotExist() {
      when(procurementItemService.getProcurementItemById(999L, "testuser"))
          .thenReturn(Optional.empty());

      ResponseEntity<ProcurementItemDTO> response = 
          controller.getProcurementItem(1L, 1L, 999L, false, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("createProcurementItem Tests")
  class CreateProcurementItemTests {

    @Test
    @DisplayName("Should create procurement item successfully")
    void shouldCreateProcurementItemSuccessfully() {
      when(procurementItemService.createProcurementItem(any(ProcurementItemDTO.class), eq("testuser")))
          .thenReturn(testItem);

      ProcurementItemDTO request = new ProcurementItemDTO();
      request.setName("New Item");
      request.setPurchaseRequisition("PR-002");

      ResponseEntity<ProcurementItemDTO> response = 
          controller.createProcurementItem(1L, 1L, request, authentication);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return bad request when creation fails")
    void shouldReturnBadRequestWhenCreationFails() {
      when(procurementItemService.createProcurementItem(any(ProcurementItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Invalid data"));

      ProcurementItemDTO request = new ProcurementItemDTO();

      ResponseEntity<ProcurementItemDTO> response = 
          controller.createProcurementItem(1L, 1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateProcurementItem Tests")
  class UpdateProcurementItemTests {

    @Test
    @DisplayName("Should update procurement item successfully")
    void shouldUpdateProcurementItemSuccessfully() {
      when(procurementItemService.updateProcurementItem(eq(1L), any(ProcurementItemDTO.class), eq("testuser")))
          .thenReturn(testItem);

      ProcurementItemDTO request = new ProcurementItemDTO();
      request.setName("Updated Name");

      ResponseEntity<ProcurementItemDTO> response = 
          controller.updateProcurementItem(1L, 1L, 1L, request, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return not found when item does not exist")
    void shouldReturnNotFoundWhenItemDoesNotExist() {
      when(procurementItemService.updateProcurementItem(eq(999L), any(ProcurementItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Procurement item not found"));

      ProcurementItemDTO request = new ProcurementItemDTO();

      ResponseEntity<ProcurementItemDTO> response = 
          controller.updateProcurementItem(1L, 1L, 999L, request, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request on validation error")
    void shouldReturnBadRequestOnValidationError() {
      when(procurementItemService.updateProcurementItem(eq(1L), any(ProcurementItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Invalid PR"));

      ProcurementItemDTO request = new ProcurementItemDTO();

      ResponseEntity<ProcurementItemDTO> response = 
          controller.updateProcurementItem(1L, 1L, 1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateProcurementItemStatus Tests")
  class UpdateStatusTests {

    @Test
    @DisplayName("Should update status successfully")
    void shouldUpdateStatusSuccessfully() {
      when(procurementItemService.updateProcurementItemStatus(1L, "PENDING_QUOTES", "testuser"))
          .thenReturn(testItem);

      // Create status update request using the inner record
      var request = new ProcurementItemController.StatusUpdateRequest("PENDING_QUOTES");

      ResponseEntity<ProcurementItemDTO> response = 
          controller.updateProcurementItemStatus(1L, 1L, 1L, request, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return not found when item does not exist")
    void shouldReturnNotFoundWhenItemDoesNotExist() {
      when(procurementItemService.updateProcurementItemStatus(999L, "APPROVED", "testuser"))
          .thenThrow(new IllegalArgumentException("Procurement item not found"));

      var request = new ProcurementItemController.StatusUpdateRequest("APPROVED");

      ResponseEntity<ProcurementItemDTO> response = 
          controller.updateProcurementItemStatus(1L, 1L, 999L, request, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request for invalid status")
    void shouldReturnBadRequestForInvalidStatus() {
      when(procurementItemService.updateProcurementItemStatus(1L, "INVALID", "testuser"))
          .thenThrow(new IllegalArgumentException("Invalid status"));

      var request = new ProcurementItemController.StatusUpdateRequest("INVALID");

      ResponseEntity<ProcurementItemDTO> response = 
          controller.updateProcurementItemStatus(1L, 1L, 1L, request, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("deleteProcurementItem Tests")
  class DeleteProcurementItemTests {

    @Test
    @DisplayName("Should delete procurement item successfully")
    void shouldDeleteProcurementItemSuccessfully() {
      doNothing().when(procurementItemService).deleteProcurementItem(1L, "testuser");

      ResponseEntity<Void> response = 
          controller.deleteProcurementItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when item does not exist")
    void shouldReturnNotFoundWhenItemDoesNotExist() {
      doThrow(new IllegalArgumentException("Procurement item not found"))
          .when(procurementItemService).deleteProcurementItem(999L, "testuser");

      ResponseEntity<Void> response = 
          controller.deleteProcurementItem(1L, 1L, 999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return forbidden when access denied")
    void shouldReturnForbiddenWhenAccessDenied() {
      doThrow(new IllegalArgumentException("Access denied"))
          .when(procurementItemService).deleteProcurementItem(1L, "testuser");

      ResponseEntity<Void> response = 
          controller.deleteProcurementItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("Quote Endpoints Tests")
  class QuoteEndpointsTests {

    @Test
    @DisplayName("Should get quotes for procurement item")
    void shouldGetQuotesForProcurementItem() {
      when(procurementItemService.getQuotesByProcurementItemId(1L, "testuser"))
          .thenReturn(Arrays.asList(testQuote));

      ResponseEntity<List<ProcurementQuoteDTO>> response = 
          controller.getQuotes(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should return not found when procurement item does not exist")
    void shouldReturnNotFoundWhenProcurementItemDoesNotExist() {
      when(procurementItemService.getQuotesByProcurementItemId(999L, "testuser"))
          .thenThrow(new IllegalArgumentException("Procurement item not found"));

      ResponseEntity<List<ProcurementQuoteDTO>> response = 
          controller.getQuotes(1L, 1L, 999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }
}
