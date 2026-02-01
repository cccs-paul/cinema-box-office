/*
 * myRC - Fiscal Year Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.myrc.dto.FiscalYearDTO;
import com.myrc.service.FiscalYearService;
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
 * Unit tests for FiscalYearController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FiscalYearController Tests")
class FiscalYearControllerTest {

  @Mock
  private FiscalYearService fiscalYearService;

  private FiscalYearController controller;
  private FiscalYearDTO testFiscalYear;

  @BeforeEach
  void setUp() {
    controller = new FiscalYearController(fiscalYearService);

    testFiscalYear = new FiscalYearDTO(
        1L,
        "FY 2025-2026",
        "Fiscal Year 2025-2026",
        1L,
        "Test RC",
        LocalDateTime.now(),
        LocalDateTime.now(),
        true,
        true,   // showCategoryFilter
        false,  // groupByCategory
        -2,     // onTargetMin
        2       // onTargetMax
    );
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Test
  @DisplayName("getFiscalYears - Returns all fiscal years")
  void getFiscalYears_ReturnsAllFiscalYears() {
    List<FiscalYearDTO> fiscalYears = Arrays.asList(testFiscalYear);
    when(fiscalYearService.getFiscalYearsByRCId(anyLong(), anyString())).thenReturn(fiscalYears);

    ResponseEntity<List<FiscalYearDTO>> response = controller.getFiscalYears(1L, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("FY 2025-2026", response.getBody().get(0).getName());
  }

  @Test
  @DisplayName("getFiscalYears - Returns 403 on access denied")
  void getFiscalYears_ReturnsForbiddenOnAccessDenied() {
    when(fiscalYearService.getFiscalYearsByRCId(anyLong(), anyString()))
        .thenThrow(new IllegalArgumentException("Access denied"));

    ResponseEntity<List<FiscalYearDTO>> response = controller.getFiscalYears(1L, null);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  @DisplayName("getFiscalYear - Returns specific fiscal year")
  void getFiscalYear_ReturnsSpecificFiscalYear() {
    when(fiscalYearService.getFiscalYearById(anyLong(), anyString()))
        .thenReturn(Optional.of(testFiscalYear));

    ResponseEntity<?> response = controller.getFiscalYear(1L, 1L, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("FY 2025-2026", ((FiscalYearDTO) response.getBody()).getName());
  }

  @Test
  @DisplayName("getFiscalYear - Returns 404 when not found")
  void getFiscalYear_ReturnsNotFoundWhenMissing() {
    when(fiscalYearService.getFiscalYearById(anyLong(), anyString())).thenReturn(Optional.empty());

    ResponseEntity<?> response = controller.getFiscalYear(1L, 999L, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("createFiscalYear - Creates new fiscal year")
  void createFiscalYear_CreatesNewFiscalYear() {
    when(fiscalYearService.createFiscalYear(anyLong(), anyString(), anyString(), anyString()))
        .thenReturn(testFiscalYear);

    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest(
            "FY 2025-2026",
            "Fiscal Year 2025-2026"
        );

    ResponseEntity<?> response = controller.createFiscalYear(1L, null, request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("FY 2025-2026", ((FiscalYearDTO) response.getBody()).getName());
  }

  @Test
  @DisplayName("createFiscalYear - Returns 400 on empty name")
  void createFiscalYear_ReturnsBadRequestOnEmptyName() {
    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest(
            "",  // Empty name
            "Description"
        );

    ResponseEntity<?> response = controller.createFiscalYear(1L, null, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("createFiscalYear - Returns 400 on null name")
  void createFiscalYear_ReturnsBadRequestOnNullName() {
    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest(
            null,  // Null name
            "Description"
        );

    ResponseEntity<?> response = controller.createFiscalYear(1L, null, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("createFiscalYear - Returns 400 on duplicate name")
  void createFiscalYear_ReturnsBadRequestOnDuplicateName() {
    when(fiscalYearService.createFiscalYear(anyLong(), anyString(), anyString(), anyString()))
        .thenThrow(new IllegalArgumentException("A Fiscal Year with this name already exists"));

    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest(
            "FY 2025-2026",
            "Description"
        );

    ResponseEntity<?> response = controller.createFiscalYear(1L, null, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("updateFiscalYear - Updates fiscal year")
  void updateFiscalYear_UpdatesExistingFiscalYear() {
    FiscalYearDTO updatedFY = new FiscalYearDTO(
        1L,
        "FY 2025-2026 Updated",
        "Updated description",
        1L,
        "Test RC",
        LocalDateTime.now(),
        LocalDateTime.now(),
        true,
        true,   // showCategoryFilter
        false,  // groupByCategory
        -2,     // onTargetMin
        2       // onTargetMax
    );

    when(fiscalYearService.updateFiscalYear(anyLong(), anyString(), anyString(), anyString()))
        .thenReturn(Optional.of(updatedFY));

    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest(
            "FY 2025-2026 Updated",
            "Updated description"
        );

    ResponseEntity<?> response = controller.updateFiscalYear(1L, 1L, null, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("FY 2025-2026 Updated", ((FiscalYearDTO) response.getBody()).getName());
  }

  @Test
  @DisplayName("updateFiscalYear - Returns 404 when not found")
  void updateFiscalYear_ReturnsNotFoundWhenMissing() {
    when(fiscalYearService.updateFiscalYear(anyLong(), anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());

    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest(
            "FY 2025-2026",
            "Description"
        );

    ResponseEntity<?> response = controller.updateFiscalYear(1L, 999L, null, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("deleteFiscalYear - Deletes fiscal year")
  void deleteFiscalYear_DeletesExistingFiscalYear() {
    doNothing().when(fiscalYearService).deleteFiscalYear(anyLong(), anyString());

    ResponseEntity<Void> response = controller.deleteFiscalYear(1L, 1L, null);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  @DisplayName("deleteFiscalYear - Returns 404 when not found")
  void deleteFiscalYear_ReturnsNotFoundWhenMissing() {
    doThrow(new IllegalArgumentException("Fiscal Year not found"))
        .when(fiscalYearService).deleteFiscalYear(anyLong(), anyString());

    ResponseEntity<Void> response = controller.deleteFiscalYear(1L, 999L, null);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @DisplayName("FiscalYearCreateRequest - getters and setters work correctly")
  void testFiscalYearCreateRequest() {
    FiscalYearController.FiscalYearCreateRequest request =
        new FiscalYearController.FiscalYearCreateRequest();

    request.setName("Test FY");
    request.setDescription("Test Description");

    assertEquals("Test FY", request.getName());
    assertEquals("Test Description", request.getDescription());
  }
}
