/*
 * myRC - Money Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for MoneyController.
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.myrc.dto.MoneyDTO;
import com.myrc.service.MoneyService;
import java.time.LocalDateTime;
import java.util.Arrays;
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

/**
 * Unit tests for MoneyController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MoneyController Tests")
class MoneyControllerTest {

  @Mock
  private MoneyService moneyService;

  private MoneyController controller;
  private MoneyDTO defaultMoney;
  private MoneyDTO customMoney;

  @BeforeEach
  void setUp() {
    controller = new MoneyController(moneyService);

    defaultMoney = new MoneyDTO(
        1L, "AB", "A-Base", "Default money", true,
        1L, "FY 2025-2026", 1L, 0,
        LocalDateTime.now(), LocalDateTime.now(), true
    );

    customMoney = new MoneyDTO(
        2L, "OA", "Operating Allotment", "Custom money", false,
        1L, "FY 2025-2026", 1L, 1,
        LocalDateTime.now(), LocalDateTime.now(), true
    );
  }

  @Test
  @DisplayName("Should create controller successfully")
  void testControllerCreation() {
    assertNotNull(controller);
  }

  @Nested
  @DisplayName("getMonies Tests")
  class GetMoniesTests {

    @Test
    @DisplayName("Returns all monies")
    void returnsAllMonies() {
      List<MoneyDTO> monies = Arrays.asList(defaultMoney, customMoney);
      when(moneyService.getMoniesByFiscalYearId(anyLong(), anyString())).thenReturn(monies);

      ResponseEntity<List<MoneyDTO>> response = controller.getMonies(1L, 1L, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().size());
      assertEquals("AB", response.getBody().get(0).getCode());
    }

    @Test
    @DisplayName("Returns 403 on access denied")
    void returnsForbiddenOnAccessDenied() {
      when(moneyService.getMoniesByFiscalYearId(anyLong(), anyString()))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<MoneyDTO>> response = controller.getMonies(1L, 1L, null);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("getMoney Tests")
  class GetMoneyTests {

    @Test
    @DisplayName("Returns specific money")
    void returnsSpecificMoney() {
      when(moneyService.getMoneyById(anyLong(), anyString()))
          .thenReturn(Optional.of(defaultMoney));

      ResponseEntity<?> response = controller.getMoney(1L, 1L, 1L, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("AB", ((MoneyDTO) response.getBody()).getCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFound() {
      when(moneyService.getMoneyById(anyLong(), anyString()))
          .thenReturn(Optional.empty());

      ResponseEntity<?> response = controller.getMoney(1L, 1L, 99L, null);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("createMoney Tests")
  class CreateMoneyTests {

    @Test
    @DisplayName("Creates money successfully")
    void createsMoney() {
      when(moneyService.createMoney(eq(1L), anyString(), eq("WCF"), eq("Working Capital Fund"), eq("Test")))
          .thenReturn(new MoneyDTO(3L, "WCF", "Working Capital Fund", "Test", false,
              1L, "FY 2025-2026", 1L, 2, LocalDateTime.now(), LocalDateTime.now(), true));

      MoneyController.MoneyCreateRequest request = new MoneyController.MoneyCreateRequest();
      request.setCode("WCF");
      request.setName("Working Capital Fund");
      request.setDescription("Test");

      ResponseEntity<?> response = controller.createMoney(1L, 1L, null, request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("WCF", ((MoneyDTO) response.getBody()).getCode());
    }

    @Test
    @DisplayName("Returns 400 when code is empty")
    void returnsBadRequestWhenCodeEmpty() {
      MoneyController.MoneyCreateRequest request = new MoneyController.MoneyCreateRequest();
      request.setCode("");
      request.setName("Test");

      ResponseEntity<?> response = controller.createMoney(1L, 1L, null, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 409 when code exists")
    void returnsConflictWhenCodeExists() {
      when(moneyService.createMoney(anyLong(), anyString(), anyString(), anyString(), any()))
          .thenThrow(new IllegalArgumentException("A Money with this code already exists"));

      MoneyController.MoneyCreateRequest request = new MoneyController.MoneyCreateRequest();
      request.setCode("AB");
      request.setName("Duplicate");

      ResponseEntity<?> response = controller.createMoney(1L, 1L, null, request);

      assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 403 when no access")
    void returnsForbiddenWhenNoAccess() {
      when(moneyService.createMoney(anyLong(), anyString(), anyString(), anyString(), any()))
          .thenThrow(new IllegalArgumentException("User does not have write access"));

      MoneyController.MoneyCreateRequest request = new MoneyController.MoneyCreateRequest();
      request.setCode("WCF");
      request.setName("Test");

      ResponseEntity<?> response = controller.createMoney(1L, 1L, null, request);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateMoney Tests")
  class UpdateMoneyTests {

    @Test
    @DisplayName("Updates money successfully")
    void updatesMoney() {
      when(moneyService.updateMoney(eq(2L), anyString(), eq("WCF"), eq("Working Capital"), eq("Updated")))
          .thenReturn(new MoneyDTO(2L, "WCF", "Working Capital", "Updated", false,
              1L, "FY 2025-2026", 1L, 1, LocalDateTime.now(), LocalDateTime.now(), true));

      MoneyController.MoneyUpdateRequest request = new MoneyController.MoneyUpdateRequest();
      request.setCode("WCF");
      request.setName("Working Capital");
      request.setDescription("Updated");

      ResponseEntity<?> response = controller.updateMoney(1L, 1L, 2L, null, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("WCF", ((MoneyDTO) response.getBody()).getCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFoundWhenNotFound() {
      when(moneyService.updateMoney(anyLong(), anyString(), any(), any(), any()))
          .thenThrow(new IllegalArgumentException("Money not found"));

      MoneyController.MoneyUpdateRequest request = new MoneyController.MoneyUpdateRequest();
      request.setName("Test");

      ResponseEntity<?> response = controller.updateMoney(1L, 1L, 99L, null, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 403 when trying to change default code")
    void returnsForbiddenWhenChangingDefaultCode() {
      when(moneyService.updateMoney(anyLong(), anyString(), any(), any(), any()))
          .thenThrow(new IllegalArgumentException("Cannot change the code of the default money"));

      MoneyController.MoneyUpdateRequest request = new MoneyController.MoneyUpdateRequest();
      request.setCode("XX");

      ResponseEntity<?> response = controller.updateMoney(1L, 1L, 1L, null, request);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("deleteMoney Tests")
  class DeleteMoneyTests {

    @Test
    @DisplayName("Deletes money successfully")
    void deletesMoney() {
      doNothing().when(moneyService).deleteMoney(eq(2L), anyString());

      ResponseEntity<?> response = controller.deleteMoney(1L, 1L, 2L, null);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 404 when not found")
    void returnsNotFoundWhenNotFound() {
      doThrow(new IllegalArgumentException("Money not found"))
          .when(moneyService).deleteMoney(eq(99L), anyString());

      ResponseEntity<?> response = controller.deleteMoney(1L, 1L, 99L, null);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 403 when trying to delete default")
    void returnsForbiddenWhenDeletingDefault() {
      doThrow(new IllegalArgumentException("Cannot delete the default money"))
          .when(moneyService).deleteMoney(eq(1L), anyString());

      ResponseEntity<?> response = controller.deleteMoney(1L, 1L, 1L, null);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 409 when money is in use")
    void returnsConflictWhenMoneyInUse() {
      doThrow(new IllegalArgumentException(
          "Cannot delete money type \"OA\" because it is in use with non-zero funding or spending allocations"))
          .when(moneyService).deleteMoney(eq(2L), anyString());

      ResponseEntity<?> response = controller.deleteMoney(1L, 1L, 2L, null);

      assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("reorderMonies Tests")
  class ReorderTests {

    @Test
    @DisplayName("Reorders monies successfully")
    void reordersMonies() {
      doNothing().when(moneyService).reorderMonies(eq(1L), anyString(), eq(Arrays.asList(2L, 1L)));

      MoneyController.MoneyReorderRequest request = new MoneyController.MoneyReorderRequest();
      request.setMoneyIds(Arrays.asList(2L, 1L));

      ResponseEntity<?> response = controller.reorderMonies(1L, 1L, null, request);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Returns 400 when moneyIds is empty")
    void returnsBadRequestWhenEmpty() {
      MoneyController.MoneyReorderRequest request = new MoneyController.MoneyReorderRequest();
      request.setMoneyIds(Arrays.asList());

      ResponseEntity<?> response = controller.reorderMonies(1L, 1L, null, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }
}
