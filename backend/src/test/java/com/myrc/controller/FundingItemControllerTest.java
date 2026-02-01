/*
 * myRC - Funding Item Controller Tests
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

import com.myrc.dto.FundingItemDTO;
import com.myrc.dto.MoneyAllocationDTO;
import com.myrc.service.FundingItemService;
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
 * Unit tests for FundingItemController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class FundingItemControllerTest {

  @Mock
  private FundingItemService fundingItemService;

  private Authentication authentication;
  private FundingItemController controller;
  private FundingItemDTO testFundingItem;

  @BeforeEach
  void setUp() {
    controller = new FundingItemController(fundingItemService);
    authentication = createAuthentication("testuser");

    testFundingItem = new FundingItemDTO();
    testFundingItem.setId(1L);
    testFundingItem.setName("Test Funding Item");
    testFundingItem.setDescription("Test Description");
    testFundingItem.setFiscalYearId(1L);
    testFundingItem.setSource("BUSINESS_PLAN");
    testFundingItem.setCurrency("CAD");
    testFundingItem.setExchangeRate(BigDecimal.ONE);
    testFundingItem.setCreatedAt(LocalDateTime.now());
    testFundingItem.setUpdatedAt(LocalDateTime.now());
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
  @DisplayName("getFundingItems Tests")
  class GetFundingItemsTests {

    @Test
    @DisplayName("Should return all funding items for fiscal year")
    void shouldReturnAllFundingItems() {
      List<FundingItemDTO> fundingItems = Arrays.asList(testFundingItem);
      when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), anyString()))
          .thenReturn(fundingItems);

      ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
      assertEquals("Test Funding Item", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("Should return 403 when access denied")
    void shouldReturn403WhenAccessDenied() {
      
      when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), anyString()))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 500 on server error")
    void shouldReturn500OnServerError() {
      
      when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), anyString()))
          .thenThrow(new RuntimeException("Database error"));

      ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, authentication);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Should use default user when no authentication")
    void shouldUseDefaultUserWhenNoAuthentication() {
      List<FundingItemDTO> fundingItems = Arrays.asList(testFundingItem);
      when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), eq("default-user")))
          .thenReturn(fundingItems);

      ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(fundingItemService).getFundingItemsByFiscalYearId(1L, "default-user");
    }

    @Test
    @DisplayName("Should return empty list when no funding items exist")
    void shouldReturnEmptyListWhenNoItems() {
      
      when(fundingItemService.getFundingItemsByFiscalYearId(anyLong(), anyString()))
          .thenReturn(Arrays.asList());

      ResponseEntity<List<FundingItemDTO>> response = controller.getFundingItems(1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isEmpty());
    }
  }

  @Nested
  @DisplayName("getFundingItem Tests")
  class GetFundingItemTests {

    @Test
    @DisplayName("Should return funding item by ID")
    void shouldReturnFundingItemById() {
      
      when(fundingItemService.getFundingItemById(anyLong(), anyString()))
          .thenReturn(Optional.of(testFundingItem));

      ResponseEntity<?> response = controller.getFundingItem(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1L, ((FundingItemDTO) response.getBody()).getId());
      assertEquals("Test Funding Item", ((FundingItemDTO) response.getBody()).getName());
    }

    @Test
    @DisplayName("Should return 404 when funding item not found")
    void shouldReturn404WhenNotFound() {
      
      when(fundingItemService.getFundingItemById(anyLong(), anyString()))
          .thenReturn(Optional.empty());

      ResponseEntity<?> response = controller.getFundingItem(1L, 999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 500 on server error")
    void shouldReturn500OnServerError() {
      
      when(fundingItemService.getFundingItemById(anyLong(), anyString()))
          .thenThrow(new RuntimeException("Database error"));

      ResponseEntity<?> response = controller.getFundingItem(1L, 1L, authentication);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("createFundingItem Tests")
  class CreateFundingItemTests {

    @Test
    @DisplayName("Should create funding item successfully")
    void shouldCreateFundingItemSuccessfully() {
      
      when(fundingItemService.createFundingItem(
          anyLong(), anyString(), anyString(), anyString(),
          any(), any(), any(), any(), any(), any()))
          .thenReturn(testFundingItem);

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "New Funding Item",
              "New Description",
              "BUSINESS_PLAN",
              "Comments",
              "CAD",
              BigDecimal.ONE,
              1L,
              null
          );

      ResponseEntity<?> response = controller.createFundingItem(1L, authentication, request);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 400 when name is empty")
    void shouldReturn400WhenNameEmpty() {
      

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "",
              "Description",
              "BUSINESS_PLAN",
              null,
              "CAD",
              null,
              null,
              null
          );

      ResponseEntity<?> response = controller.createFundingItem(1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 when name is null")
    void shouldReturn400WhenNameNull() {
      

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              null,
              "Description",
              "BUSINESS_PLAN",
              null,
              "CAD",
              null,
              null,
              null
          );

      ResponseEntity<?> response = controller.createFundingItem(1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 on IllegalArgumentException")
    void shouldReturn400OnIllegalArgumentException() {
      
      when(fundingItemService.createFundingItem(
          anyLong(), anyString(), anyString(), anyString(),
          any(), any(), any(), any(), any(), any()))
          .thenThrow(new IllegalArgumentException("Invalid data"));

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "New Funding Item",
              "Description",
              "INVALID_SOURCE",
              null,
              "CAD",
              null,
              null,
              null
          );

      ResponseEntity<?> response = controller.createFundingItem(1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 500 on server error")
    void shouldReturn500OnServerError() {
      
      when(fundingItemService.createFundingItem(
          anyLong(), anyString(), anyString(), anyString(),
          any(), any(), any(), any(), any(), any()))
          .thenThrow(new RuntimeException("Database error"));

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "New Funding Item",
              "Description",
              "BUSINESS_PLAN",
              null,
              "CAD",
              null,
              null,
              null
          );

      ResponseEntity<?> response = controller.createFundingItem(1L, authentication, request);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("updateFundingItem Tests")
  class UpdateFundingItemTests {

    @Test
    @DisplayName("Should update funding item successfully")
    void shouldUpdateFundingItemSuccessfully() {
      
      when(fundingItemService.updateFundingItem(
          anyLong(), anyString(), anyString(), anyString(),
          any(), any(), any(), any(), any(), any()))
          .thenReturn(Optional.of(testFundingItem));

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "Updated Funding Item",
              "Updated Description",
              "BUSINESS_PLAN",
              "Comments",
              "CAD",
              BigDecimal.ONE,
              1L,
              null
          );

      ResponseEntity<?> response = controller.updateFundingItem(1L, 1L, authentication, request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 404 when funding item not found")
    void shouldReturn404WhenNotFound() {
      
      when(fundingItemService.updateFundingItem(
          anyLong(), anyString(), anyString(), anyString(),
          any(), any(), any(), any(), any(), any()))
          .thenReturn(Optional.empty());

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "Updated Name",
              "Description",
              "BUSINESS_PLAN",
              null,
              "CAD",
              null,
              null,
              null
          );

      ResponseEntity<?> response = controller.updateFundingItem(1L, 999L, authentication, request);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 on IllegalArgumentException")
    void shouldReturn400OnIllegalArgumentException() {
      
      when(fundingItemService.updateFundingItem(
          anyLong(), anyString(), anyString(), anyString(),
          any(), any(), any(), any(), any(), any()))
          .thenThrow(new IllegalArgumentException("Invalid data"));

      FundingItemController.FundingItemCreateRequest request = 
          new FundingItemController.FundingItemCreateRequest(
              "Updated Name",
              "Description",
              "INVALID_SOURCE",
              null,
              "CAD",
              null,
              null,
              null
          );

      ResponseEntity<?> response = controller.updateFundingItem(1L, 1L, authentication, request);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("deleteFundingItem Tests")
  class DeleteFundingItemTests {

    @Test
    @DisplayName("Should delete funding item successfully")
    void shouldDeleteFundingItemSuccessfully() {
      
      doNothing().when(fundingItemService).deleteFundingItem(anyLong(), anyString());

      ResponseEntity<Void> response = controller.deleteFundingItem(1L, 1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
      verify(fundingItemService).deleteFundingItem(1L, "testuser");
    }

    @Test
    @DisplayName("Should return 404 when funding item not found")
    void shouldReturn404WhenNotFound() {
      
      doThrow(new IllegalArgumentException("Funding item not found"))
          .when(fundingItemService).deleteFundingItem(anyLong(), anyString());

      ResponseEntity<Void> response = controller.deleteFundingItem(1L, 999L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 500 on server error")
    void shouldReturn500OnServerError() {
      
      doThrow(new RuntimeException("Database error"))
          .when(fundingItemService).deleteFundingItem(anyLong(), anyString());

      ResponseEntity<Void> response = controller.deleteFundingItem(1L, 1L, authentication);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
  }
}
