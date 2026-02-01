/*
 * myRC - Spending Item Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for SpendingItemController.
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.myrc.dto.SpendingItemDTO;
import com.myrc.dto.SpendingMoneyAllocationDTO;
import com.myrc.service.SpendingItemService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * Unit tests for SpendingItemController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpendingItemController Tests")
class SpendingItemControllerTest {

  @Mock
  private SpendingItemService spendingItemService;

  private SpendingItemController controller;
  private SpendingItemDTO gpuPurchase;
  private SpendingItemDTO softwareLicense;
  private SpendingMoneyAllocationDTO allocationDTO;
  private Authentication authentication;

  /**
   * Simple test implementation of Authentication.
   */
  private static class TestAuthentication implements Authentication {
    private final String name;

    TestAuthentication(String name) {
      this.name = name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      return name;
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
      // No-op for testing
    }

    @Override
    public String getName() {
      return name;
    }
  }

  @BeforeEach
  void setUp() {
    controller = new SpendingItemController(spendingItemService);
    authentication = new TestAuthentication("testuser");

    allocationDTO = new SpendingMoneyAllocationDTO(
        1L, 1L, "A-Base", true,
        new BigDecimal("50000"), BigDecimal.ZERO, new BigDecimal("50000"),
        LocalDateTime.now(), LocalDateTime.now()
    );

    gpuPurchase = new SpendingItemDTO(
        1L, "GPU Purchase", "Purchase of NVIDIA A100 GPUs", "NVIDIA", "PO-001",
        new BigDecimal("50000"), "DRAFT", "CAD", null,
        2L, "GPUs", 1L, "FY 2025-2026", 1L, "Demo RC",
        LocalDateTime.now(), LocalDateTime.now(), true,
        Arrays.asList(allocationDTO)
    );

    softwareLicense = new SpendingItemDTO(
        2L, "Software License", "Annual software licenses", "Microsoft", "INV-002",
        new BigDecimal("25000"), "PENDING", "CAD", null,
        4L, "Software Licenses", 1L, "FY 2025-2026", 1L, "Demo RC",
        LocalDateTime.now(), LocalDateTime.now(), true,
        Arrays.asList(allocationDTO)
    );
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Nested
  @DisplayName("getSpendingItems Tests")
  class GetSpendingItemsTests {

    @Test
    @DisplayName("Returns all spending items")
    void returnsAllItems() {
      List<SpendingItemDTO> items = Arrays.asList(gpuPurchase, softwareLicense);
      when(spendingItemService.getSpendingItemsByFiscalYearId(anyLong(), anyString())).thenReturn(items);

      ResponseEntity<List<SpendingItemDTO>> response = controller.getSpendingItems(1L, 1L, null, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().size());
      assertEquals("GPU Purchase", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("Returns spending items filtered by category")
    void returnsItemsFilteredByCategory() {
      List<SpendingItemDTO> items = Arrays.asList(gpuPurchase);
      when(spendingItemService.getSpendingItemsByFiscalYearIdAndCategoryId(eq(1L), eq(2L), anyString()))
          .thenReturn(items);

      ResponseEntity<List<SpendingItemDTO>> response = controller.getSpendingItems(1L, 1L, 2L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
      assertEquals("GPUs", response.getBody().get(0).getCategoryName());
    }

    @Test
    @DisplayName("Returns 403 on access denied")
    void returnsForbiddenOnAccessDenied() {
      when(spendingItemService.getSpendingItemsByFiscalYearId(anyLong(), anyString()))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<SpendingItemDTO>> response = controller.getSpendingItems(1L, 1L, null, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getSpendingItem Tests")
  class GetSpendingItemTests {

    @Test
    @DisplayName("Returns specific spending item")
    void returnsSpecificItem() {
      when(spendingItemService.getSpendingItemById(anyLong(), anyString()))
          .thenReturn(Optional.of(gpuPurchase));

      ResponseEntity<SpendingItemDTO> response = controller.getSpendingItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("GPU Purchase", response.getBody().getName());
      assertEquals("DRAFT", response.getBody().getStatus());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      when(spendingItemService.getSpendingItemById(anyLong(), anyString()))
          .thenReturn(Optional.empty());

      ResponseEntity<SpendingItemDTO> response = controller.getSpendingItem(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("createSpendingItem Tests")
  class CreateSpendingItemTests {

    @Test
    @DisplayName("Creates spending item successfully")
    void createsItem() {
      SpendingItemDTO request = new SpendingItemDTO();
      request.setName("New Purchase");
      request.setDescription("New purchase description");
      request.setCategoryId(2L);
      request.setAmount(new BigDecimal("10000"));
      request.setCurrency("CAD");

      when(spendingItemService.createSpendingItem(any(SpendingItemDTO.class), anyString()))
          .thenReturn(gpuPurchase);

      ResponseEntity<?> response = controller.createSpendingItem(1L, 1L, authentication, request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 400 on invalid data")
    void returnsBadRequest() {
      SpendingItemDTO request = new SpendingItemDTO();
      request.setName("");

      when(spendingItemService.createSpendingItem(any(SpendingItemDTO.class), anyString()))
          .thenThrow(new IllegalArgumentException("Spending item name is required"));

      ResponseEntity<?> response = controller.createSpendingItem(1L, 1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateSpendingItem Tests")
  class UpdateSpendingItemTests {

    @Test
    @DisplayName("Updates spending item successfully")
    void updatesItem() {
      SpendingItemDTO request = new SpendingItemDTO();
      request.setName("Updated Purchase");
      request.setDescription("Updated description");

      when(spendingItemService.updateSpendingItem(eq(1L), any(SpendingItemDTO.class), anyString()))
          .thenReturn(gpuPurchase);

      ResponseEntity<?> response = controller.updateSpendingItem(1L, 1L, 1L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      SpendingItemDTO request = new SpendingItemDTO();
      request.setName("Updated");

      when(spendingItemService.updateSpendingItem(eq(99L), any(SpendingItemDTO.class), anyString()))
          .thenThrow(new IllegalArgumentException("Spending item not found"));

      ResponseEntity<?> response = controller.updateSpendingItem(1L, 1L, 99L, authentication, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("deleteSpendingItem Tests")
  class DeleteSpendingItemTests {

    @Test
    @DisplayName("Deletes spending item successfully")
    void deletesItem() {
      doNothing().when(spendingItemService).deleteSpendingItem(eq(1L), anyString());

      ResponseEntity<?> response = controller.deleteSpendingItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      doThrow(new IllegalArgumentException("Spending item not found"))
          .when(spendingItemService).deleteSpendingItem(eq(99L), anyString());

      ResponseEntity<?> response = controller.deleteSpendingItem(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 400 when deleting approved item")
    void returnsBadRequestOnDeleteApproved() {
      doThrow(new IllegalArgumentException("Cannot delete a spending item with status APPROVED"))
          .when(spendingItemService).deleteSpendingItem(eq(1L), anyString());

      ResponseEntity<?> response = controller.deleteSpendingItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateStatus Tests")
  class UpdateStatusTests {

    @Test
    @DisplayName("Updates status successfully")
    void updatesStatus() {
      SpendingItemDTO updatedItem = new SpendingItemDTO();
      updatedItem.setId(1L);
      updatedItem.setStatus("PENDING");

      SpendingItemController.StatusUpdateRequest request = new SpendingItemController.StatusUpdateRequest();
      request.status = "PENDING";

      when(spendingItemService.updateSpendingItemStatus(eq(1L), eq("PENDING"), anyString()))
          .thenReturn(updatedItem);

      ResponseEntity<?> response = controller.updateStatus(1L, 1L, 1L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 400 on invalid status")
    void returnsBadRequestOnInvalidStatus() {
      SpendingItemController.StatusUpdateRequest request = new SpendingItemController.StatusUpdateRequest();
      request.status = "INVALID";

      when(spendingItemService.updateSpendingItemStatus(eq(1L), eq("INVALID"), anyString()))
          .thenThrow(new IllegalArgumentException("Invalid status: INVALID"));

      ResponseEntity<?> response = controller.updateStatus(1L, 1L, 1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      SpendingItemController.StatusUpdateRequest request = new SpendingItemController.StatusUpdateRequest();
      request.status = "PENDING";

      when(spendingItemService.updateSpendingItemStatus(eq(99L), anyString(), anyString()))
          .thenThrow(new IllegalArgumentException("Spending item not found"));

      ResponseEntity<?> response = controller.updateStatus(1L, 1L, 99L, authentication, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getAllocations Tests")
  class GetAllocationsTests {

    @Test
    @DisplayName("Returns allocations successfully")
    void returnsAllocations() {
      List<SpendingMoneyAllocationDTO> allocations = Arrays.asList(allocationDTO);
      when(spendingItemService.getMoneyAllocations(eq(1L), anyString()))
          .thenReturn(allocations);

      ResponseEntity<?> response = controller.getAllocations(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      when(spendingItemService.getMoneyAllocations(eq(99L), anyString()))
          .thenThrow(new IllegalArgumentException("Spending item not found"));

      ResponseEntity<?> response = controller.getAllocations(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateAllocations Tests")
  class UpdateAllocationsTests {

    @Test
    @DisplayName("Updates allocations successfully")
    void updatesAllocations() {
      SpendingMoneyAllocationDTO newAllocation = new SpendingMoneyAllocationDTO();
      newAllocation.setMoneyId(1L);
      newAllocation.setCapAmount(new BigDecimal("30000"));
      newAllocation.setOmAmount(new BigDecimal("10000"));

      when(spendingItemService.updateMoneyAllocations(eq(1L), any(), anyString()))
          .thenReturn(gpuPurchase);

      ResponseEntity<?> response = controller.updateAllocations(
          1L, 1L, 1L, authentication, Arrays.asList(newAllocation));

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Returns 400 when no valid allocations")
    void returnsBadRequestOnNoValidAllocations() {
      SpendingMoneyAllocationDTO zeroAllocation = new SpendingMoneyAllocationDTO();
      zeroAllocation.setMoneyId(1L);
      zeroAllocation.setCapAmount(BigDecimal.ZERO);
      zeroAllocation.setOmAmount(BigDecimal.ZERO);

      when(spendingItemService.updateMoneyAllocations(eq(1L), any(), anyString()))
          .thenThrow(new IllegalArgumentException("At least one money type must have a CAP or OM amount"));

      ResponseEntity<?> response = controller.updateAllocations(
          1L, 1L, 1L, authentication, Arrays.asList(zeroAllocation));

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      SpendingMoneyAllocationDTO allocation = new SpendingMoneyAllocationDTO();
      allocation.setMoneyId(1L);
      allocation.setCapAmount(new BigDecimal("10000"));

      when(spendingItemService.updateMoneyAllocations(eq(99L), any(), anyString()))
          .thenThrow(new IllegalArgumentException("Spending item not found"));

      ResponseEntity<?> response = controller.updateAllocations(
          1L, 1L, 99L, authentication, Arrays.asList(allocation));

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }
}
