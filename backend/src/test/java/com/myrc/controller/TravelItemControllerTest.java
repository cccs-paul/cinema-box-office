/*
 * myRC - Travel Item Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.TravelItemDTO;
import com.myrc.dto.TravelMoneyAllocationDTO;
import com.myrc.service.TravelItemService;
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
 * Unit tests for TravelItemController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@ExtendWith(MockitoExtension.class)
class TravelItemControllerTest {

  @Mock
  private TravelItemService travelItemService;

  private Authentication authentication;
  private TravelItemController controller;
  private TravelItemDTO testItem;

  @BeforeEach
  void setUp() {
    controller = new TravelItemController(travelItemService);
    authentication = createAuthentication("testuser");

    testItem = new TravelItemDTO();
    testItem.setId(1L);
    testItem.setName("Ottawa Conference Trip");
    testItem.setDescription("Annual government technology conference");
    testItem.setFiscalYearId(1L);
    testItem.setStatus("PLANNED");
    testItem.setTravelType("DOMESTIC");
    testItem.setEmap("EMAP-2025-001");
    testItem.setDestination("Ottawa, ON");
    testItem.setNumberOfTravellers(2);
  }

  private Authentication createAuthentication(String username) {
    return new Authentication() {
      @Override public String getName() { return username; }
      @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
      @Override public Object getCredentials() { return null; }
      @Override public Object getDetails() { return null; }
      @Override public Object getPrincipal() { return username; }
      @Override public boolean isAuthenticated() { return true; }
      @Override public void setAuthenticated(boolean b) { }
    };
  }

  @Nested
  @DisplayName("GET /travel-items")
  class GetTravelItems {

    @Test
    @DisplayName("Should return travel items successfully")
    void shouldReturnTravelItemsSuccessfully() {
      when(travelItemService.getTravelItemsByFiscalYearId(eq(1L), eq("testuser")))
          .thenReturn(List.of(testItem));

      ResponseEntity<List<TravelItemDTO>> response = controller.getTravelItems(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
      assertEquals("Ottawa Conference Trip", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("Should return empty list when no travel items exist")
    void shouldReturnEmptyList() {
      when(travelItemService.getTravelItemsByFiscalYearId(eq(1L), eq("testuser")))
          .thenReturn(Collections.emptyList());

      ResponseEntity<List<TravelItemDTO>> response = controller.getTravelItems(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should return forbidden when no access")
    void shouldReturnForbiddenWhenNoAccess() {
      when(travelItemService.getTravelItemsByFiscalYearId(eq(1L), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<TravelItemDTO>> response = controller.getTravelItems(1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("GET /travel-items/{id}")
  class GetTravelItem {

    @Test
    @DisplayName("Should return travel item by id")
    void shouldReturnTravelItemById() {
      when(travelItemService.getTravelItemById(eq(1L), eq("testuser")))
          .thenReturn(Optional.of(testItem));

      ResponseEntity<TravelItemDTO> response = controller.getTravelItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("Ottawa Conference Trip", response.getBody().getName());
    }

    @Test
    @DisplayName("Should return not found when item does not exist")
    void shouldReturnNotFound() {
      when(travelItemService.getTravelItemById(eq(99L), eq("testuser")))
          .thenReturn(Optional.empty());

      ResponseEntity<TravelItemDTO> response = controller.getTravelItem(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /travel-items")
  class CreateTravelItem {

    @Test
    @DisplayName("Should create travel item successfully")
    void shouldCreateTravelItemSuccessfully() {
      when(travelItemService.createTravelItem(any(TravelItemDTO.class), eq("testuser")))
          .thenReturn(testItem);

      ResponseEntity<?> response = controller.createTravelItem(1L, 1L, authentication, testItem);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return bad request for invalid data")
    void shouldReturnBadRequestForInvalidData() {
      when(travelItemService.createTravelItem(any(TravelItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Name is required"));

      ResponseEntity<?> response = controller.createTravelItem(1L, 1L, authentication, testItem);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("PUT /travel-items/{id}")
  class UpdateTravelItem {

    @Test
    @DisplayName("Should update travel item successfully")
    void shouldUpdateTravelItemSuccessfully() {
      when(travelItemService.updateTravelItem(eq(1L), any(TravelItemDTO.class), eq("testuser")))
          .thenReturn(testItem);

      ResponseEntity<?> response = controller.updateTravelItem(1L, 1L, 1L, authentication, testItem);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent item")
    void shouldReturnNotFoundForNonExistentItem() {
      when(travelItemService.updateTravelItem(eq(99L), any(TravelItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Travel item not found"));

      ResponseEntity<?> response = controller.updateTravelItem(1L, 1L, 99L, authentication, testItem);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("DELETE /travel-items/{id}")
  class DeleteTravelItem {

    @Test
    @DisplayName("Should delete travel item successfully")
    void shouldDeleteTravelItemSuccessfully() {
      doNothing().when(travelItemService).deleteTravelItem(eq(1L), eq("testuser"));

      ResponseEntity<?> response = controller.deleteTravelItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent item")
    void shouldReturnNotFoundForNonExistentItem() {
      doThrow(new IllegalArgumentException("Travel item not found"))
          .when(travelItemService).deleteTravelItem(eq(99L), eq("testuser"));

      ResponseEntity<?> response = controller.deleteTravelItem(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("PUT /travel-items/{id}/status")
  class UpdateTravelItemStatus {

    @Test
    @DisplayName("Should update status successfully")
    void shouldUpdateStatusSuccessfully() {
      when(travelItemService.updateTravelItemStatus(eq(1L), eq("APPROVED"), eq("testuser")))
          .thenReturn(testItem);

      TravelItemController.StatusUpdateRequest statusRequest = new TravelItemController.StatusUpdateRequest();
      statusRequest.status = "APPROVED";

      ResponseEntity<?> response = controller.updateStatus(1L, 1L, 1L, authentication, statusRequest);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("GET /travel-items/{id}/allocations")
  class GetMoneyAllocations {

    @Test
    @DisplayName("Should return allocations successfully")
    void shouldReturnAllocationsSuccessfully() {
      TravelMoneyAllocationDTO alloc = new TravelMoneyAllocationDTO();
      alloc.setId(1L);
      alloc.setOmAmount(new java.math.BigDecimal("1500.00"));

      when(travelItemService.getMoneyAllocations(eq(1L), eq("testuser")))
          .thenReturn(List.of(alloc));

      ResponseEntity<?> response = controller.getAllocations(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }
}
