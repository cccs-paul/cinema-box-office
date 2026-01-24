/*
 * myRC - Funding Item Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.boxoffice.dto.FundingItemDTO;
import com.boxoffice.service.FundingItemService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for FundingItemController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FundingItemController Tests")
class FundingItemControllerTest {

  @Mock
  private FundingItemService fundingItemService;

  private FundingItemController controller;
  private FundingItemDTO testFundingItem;

  @BeforeEach
  void setUp() {
    controller = new FundingItemController(fundingItemService);

    testFundingItem = new FundingItemDTO(
        1L,
        "Test Funding Item",
        "Test description",
        new BigDecimal("10000.00"),
        "DRAFT",
        "CAD",
        null,
        1L,
        "FY 2025-2026",
        1L,
        "Test RC",
        LocalDateTime.now(),
        LocalDateTime.now(),
        true
    );
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Test
  @DisplayName("getFundingItems - Returns all funding items for a fiscal year")
  void getFundingItems_ReturnsAllFundingItems() {
    List<FundingItemDTO> fundingItems = Arrays.asList(testFundingItem);
    when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), anyString()))
        .thenReturn(fundingItems);

    ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("Test Funding Item", response.getBody().get(0).getName());
  }

  @Test
  @DisplayName("getFundingItems - Returns 403 on access denied")
  void getFundingItems_ReturnsForbiddenOnAccessDenied() {
    when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), anyString()))
        .thenThrow(new IllegalArgumentException("Access denied"));

    ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, null);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  @DisplayName("getFundingItem - Returns specific funding item")
  void getFundingItem_ReturnsSpecificFundingItem() {
    when(fundingItemService.getFundingItemById(anyLong(), anyString()))
        .thenReturn(Optional.of(testFundingItem));

    ResponseEntity<FundingItemDTO> response = controller.getFundingItem(1L, 1L, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Test Funding Item", response.getBody().getName());
  }

  @Test
  @DisplayName("getFundingItem - Returns 404 when not found")
  void getFundingItem_ReturnsNotFoundWhenMissing() {
    when(fundingItemService.getFundingItemById(anyLong(), anyString()))
        .thenReturn(Optional.empty());

    ResponseEntity<FundingItemDTO> response = controller.getFundingItem(1L, 999L, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("createFundingItem - Creates new funding item")
  void createFundingItem_CreatesNewFundingItem() {
    when(fundingItemService.createFundingItem(anyLong(), anyString(), anyString(), anyString(),
        any(), anyString(), anyString(), any()))
        .thenReturn(testFundingItem);

    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest(
            "Test Funding Item",
            "Test description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        );

    ResponseEntity<FundingItemDTO> response = controller.createFundingItem(1L, null, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Test Funding Item", response.getBody().getName());
  }

  @Test
  @DisplayName("createFundingItem - Returns 400 on empty name")
  void createFundingItem_ReturnsBadRequestOnEmptyName() {
    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest(
            "",  // Empty name
            "Description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        );

    ResponseEntity<FundingItemDTO> response = controller.createFundingItem(1L, null, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("createFundingItem - Returns 400 on null name")
  void createFundingItem_ReturnsBadRequestOnNullName() {
    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest(
            null,  // Null name
            "Description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        );

    ResponseEntity<FundingItemDTO> response = controller.createFundingItem(1L, null, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("createFundingItem - Returns 400 on duplicate name")
  void createFundingItem_ReturnsBadRequestOnDuplicateName() {
    when(fundingItemService.createFundingItem(anyLong(), anyString(), anyString(), anyString(),
        any(), anyString(), anyString(), any()))
        .thenThrow(new IllegalArgumentException("A Funding Item with this name already exists"));

    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest(
            "Test Funding Item",
            "Description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        );

    ResponseEntity<FundingItemDTO> response = controller.createFundingItem(1L, null, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("updateFundingItem - Updates funding item")
  void updateFundingItem_UpdatesExistingFundingItem() {
    FundingItemDTO updatedItem = new FundingItemDTO(
        1L,
        "Updated Funding Item",
        "Updated description",
        new BigDecimal("20000.00"),
        "APPROVED",
        "CAD",
        null,
        1L,
        "FY 2025-2026",
        1L,
        "Test RC",
        LocalDateTime.now(),
        LocalDateTime.now(),
        true
    );

    when(fundingItemService.updateFundingItem(anyLong(), anyString(), anyString(), anyString(),
        any(), anyString(), anyString(), any()))
        .thenReturn(Optional.of(updatedItem));

    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest(
            "Updated Funding Item",
            "Updated description",
            new BigDecimal("20000.00"),
            "APPROVED",
            "CAD",
            null
        );

    ResponseEntity<FundingItemDTO> response = controller.updateFundingItem(1L, 1L, null, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Updated Funding Item", response.getBody().getName());
  }

  @Test
  @DisplayName("updateFundingItem - Returns 404 when not found")
  void updateFundingItem_ReturnsNotFoundWhenMissing() {
    when(fundingItemService.updateFundingItem(anyLong(), anyString(), anyString(), anyString(),
        any(), anyString(), anyString(), any()))
        .thenReturn(Optional.empty());

    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest(
            "Test Funding Item",
            "Description",
            new BigDecimal("10000.00"),
            "DRAFT",
            "CAD",
            null
        );

    ResponseEntity<FundingItemDTO> response = controller.updateFundingItem(1L, 999L, null, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("deleteFundingItem - Deletes funding item")
  void deleteFundingItem_DeletesExistingFundingItem() {
    doNothing().when(fundingItemService).deleteFundingItem(anyLong(), anyString());

    ResponseEntity<Void> response = controller.deleteFundingItem(1L, 1L, null);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  @DisplayName("deleteFundingItem - Returns 404 when not found")
  void deleteFundingItem_ReturnsNotFoundWhenMissing() {
    doThrow(new IllegalArgumentException("Funding Item not found"))
        .when(fundingItemService).deleteFundingItem(anyLong(), anyString());

    ResponseEntity<Void> response = controller.deleteFundingItem(1L, 999L, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("FundingItemCreateRequest - getters and setters work correctly")
  void testFundingItemCreateRequest() {
    FundingItemController.FundingItemCreateRequest request =
        new FundingItemController.FundingItemCreateRequest();

    request.setName("Test FI");
    request.setDescription("Test Description");
    request.setBudgetAmount(new BigDecimal("15000.00"));
    request.setStatus("PENDING");

    assertEquals("Test FI", request.getName());
    assertEquals("Test Description", request.getDescription());
    assertEquals(new BigDecimal("15000.00"), request.getBudgetAmount());
    assertEquals("PENDING", request.getStatus());
  }
}
