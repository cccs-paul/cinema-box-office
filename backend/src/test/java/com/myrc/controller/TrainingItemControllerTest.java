/*
 * myRC - Training Item Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.TrainingItemDTO;
import com.myrc.dto.TrainingMoneyAllocationDTO;
import com.myrc.service.TrainingItemService;
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
 * Unit tests for TrainingItemController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@ExtendWith(MockitoExtension.class)
class TrainingItemControllerTest {

  @Mock
  private TrainingItemService trainingItemService;

  private Authentication authentication;
  private TrainingItemController controller;
  private TrainingItemDTO testItem;

  @BeforeEach
  void setUp() {
    controller = new TrainingItemController(trainingItemService);
    authentication = createAuthentication("testuser");

    testItem = new TrainingItemDTO();
    testItem.setId(1L);
    testItem.setName("Java Certification");
    testItem.setDescription("Oracle Java SE certification course");
    testItem.setFiscalYearId(1L);
    testItem.setStatus("PLANNED");
    testItem.setTrainingType("CERTIFICATION");
    testItem.setCurrency("CAD");
    testItem.setEstimatedCost(new java.math.BigDecimal("2500.00"));
    testItem.setNumberOfParticipants(3);
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
  @DisplayName("GET /training-items")
  class GetTrainingItems {

    @Test
    @DisplayName("Should return training items successfully")
    void shouldReturnTrainingItemsSuccessfully() {
      when(trainingItemService.getTrainingItemsByFiscalYearId(eq(1L), eq("testuser")))
          .thenReturn(List.of(testItem));

      ResponseEntity<List<TrainingItemDTO>> response = controller.getTrainingItems(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
      assertEquals("Java Certification", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("Should return empty list when no training items exist")
    void shouldReturnEmptyList() {
      when(trainingItemService.getTrainingItemsByFiscalYearId(eq(1L), eq("testuser")))
          .thenReturn(Collections.emptyList());

      ResponseEntity<List<TrainingItemDTO>> response = controller.getTrainingItems(1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should return forbidden when no access")
    void shouldReturnForbiddenWhenNoAccess() {
      when(trainingItemService.getTrainingItemsByFiscalYearId(eq(1L), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Access denied"));

      ResponseEntity<List<TrainingItemDTO>> response = controller.getTrainingItems(1L, 1L, authentication);

      assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("GET /training-items/{id}")
  class GetTrainingItem {

    @Test
    @DisplayName("Should return training item by id")
    void shouldReturnTrainingItemById() {
      when(trainingItemService.getTrainingItemById(eq(1L), eq("testuser")))
          .thenReturn(Optional.of(testItem));

      ResponseEntity<TrainingItemDTO> response = controller.getTrainingItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertEquals("Java Certification", response.getBody().getName());
    }

    @Test
    @DisplayName("Should return not found when item does not exist")
    void shouldReturnNotFound() {
      when(trainingItemService.getTrainingItemById(eq(99L), eq("testuser")))
          .thenReturn(Optional.empty());

      ResponseEntity<TrainingItemDTO> response = controller.getTrainingItem(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("POST /training-items")
  class CreateTrainingItem {

    @Test
    @DisplayName("Should create training item successfully")
    void shouldCreateTrainingItemSuccessfully() {
      when(trainingItemService.createTrainingItem(any(TrainingItemDTO.class), eq("testuser")))
          .thenReturn(testItem);

      ResponseEntity<?> response = controller.createTrainingItem(1L, 1L, authentication, testItem);

      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return bad request for invalid data")
    void shouldReturnBadRequestForInvalidData() {
      when(trainingItemService.createTrainingItem(any(TrainingItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Name is required"));

      ResponseEntity<?> response = controller.createTrainingItem(1L, 1L, authentication, testItem);

      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("PUT /training-items/{id}")
  class UpdateTrainingItem {

    @Test
    @DisplayName("Should update training item successfully")
    void shouldUpdateTrainingItemSuccessfully() {
      when(trainingItemService.updateTrainingItem(eq(1L), any(TrainingItemDTO.class), eq("testuser")))
          .thenReturn(testItem);

      ResponseEntity<?> response = controller.updateTrainingItem(1L, 1L, 1L, authentication, testItem);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when updating non-existent item")
    void shouldReturnNotFoundForNonExistentItem() {
      when(trainingItemService.updateTrainingItem(eq(99L), any(TrainingItemDTO.class), eq("testuser")))
          .thenThrow(new IllegalArgumentException("Training item not found"));

      ResponseEntity<?> response = controller.updateTrainingItem(1L, 1L, 99L, authentication, testItem);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("DELETE /training-items/{id}")
  class DeleteTrainingItem {

    @Test
    @DisplayName("Should delete training item successfully")
    void shouldDeleteTrainingItemSuccessfully() {
      doNothing().when(trainingItemService).deleteTrainingItem(eq(1L), eq("testuser"));

      ResponseEntity<?> response = controller.deleteTrainingItem(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent item")
    void shouldReturnNotFoundForNonExistentItem() {
      doThrow(new IllegalArgumentException("Training item not found"))
          .when(trainingItemService).deleteTrainingItem(eq(99L), eq("testuser"));

      ResponseEntity<?> response = controller.deleteTrainingItem(1L, 1L, 99L, authentication);

      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("PUT /training-items/{id}/status")
  class UpdateTrainingItemStatus {

    @Test
    @DisplayName("Should update status successfully")
    void shouldUpdateStatusSuccessfully() {
      when(trainingItemService.updateTrainingItemStatus(eq(1L), eq("APPROVED"), eq("testuser")))
          .thenReturn(testItem);

      TrainingItemController.StatusUpdateRequest statusRequest = new TrainingItemController.StatusUpdateRequest();
      statusRequest.status = "APPROVED";

      ResponseEntity<?> response = controller.updateStatus(1L, 1L, 1L, authentication, statusRequest);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }

  @Nested
  @DisplayName("GET /training-items/{id}/allocations")
  class GetMoneyAllocations {

    @Test
    @DisplayName("Should return allocations successfully")
    void shouldReturnAllocationsSuccessfully() {
      TrainingMoneyAllocationDTO alloc = new TrainingMoneyAllocationDTO();
      alloc.setId(1L);
      alloc.setOmAmount(new java.math.BigDecimal("1000.00"));

      when(trainingItemService.getMoneyAllocations(eq(1L), eq("testuser")))
          .thenReturn(List.of(alloc));

      ResponseEntity<?> response = controller.getAllocations(1L, 1L, 1L, authentication);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }
}
