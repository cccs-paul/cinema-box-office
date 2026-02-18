/*
 * myRC - Training Item Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.TrainingItemDTO;
import com.myrc.dto.TrainingMoneyAllocationDTO;
import com.myrc.model.*;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.TrainingItemRepository;
import com.myrc.repository.TrainingMoneyAllocationRepository;
import com.myrc.repository.TrainingParticipantRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for TrainingItemServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingItemService Tests")
class TrainingItemServiceTest {

  @Mock
  private TrainingItemRepository trainingItemRepository;

  @Mock
  private TrainingMoneyAllocationRepository allocationRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private MoneyRepository moneyRepository;

  @Mock
  private RCPermissionService permissionService;

  @Mock
  private TrainingParticipantRepository participantRepository;

  private TrainingItemServiceImpl trainingItemService;

  private User testUser;
  private ResponsibilityCentre rc;
  private FiscalYear fy;
  private TrainingItem testTrainingItem;
  private Money aBaseMoney;

  @BeforeEach
  void setUp() {
    trainingItemService = new TrainingItemServiceImpl(
        trainingItemRepository,
        allocationRepository,
        participantRepository,
        fiscalYearRepository,
        moneyRepository,
        permissionService
    );

    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    rc = new ResponsibilityCentre();
    rc.setId(1L);
    rc.setName("Demo RC");
    rc.setOwner(testUser);

    fy = new FiscalYear();
    fy.setId(1L);
    fy.setName("FY 2025-2026");
    fy.setResponsibilityCentre(rc);

    aBaseMoney = new Money();
    aBaseMoney.setId(1L);
    aBaseMoney.setCode("AB");
    aBaseMoney.setName("A-Base");
    aBaseMoney.setIsDefault(true);
    aBaseMoney.setFiscalYear(fy);

    testTrainingItem = new TrainingItem();
    testTrainingItem.setId(1L);
    testTrainingItem.setName("Java Certification");
    testTrainingItem.setDescription("Oracle Java SE certification");
    testTrainingItem.setProvider("Oracle");
    testTrainingItem.setStatus(TrainingItem.Status.PLANNED);
    testTrainingItem.setTrainingType(TrainingItem.TrainingType.COURSE_TRAINING);
    testTrainingItem.setFormat(TrainingItem.TrainingFormat.IN_PERSON);
    testTrainingItem.setFiscalYear(fy);
    testTrainingItem.setMoneyAllocations(new ArrayList<>());

    lenient().when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(true);
    lenient().when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(true);
  }

  @Nested
  @DisplayName("getTrainingItemsByFiscalYearId")
  class GetTrainingItemsByFiscalYearId {

    @Test
    @DisplayName("Should return all training items for fiscal year")
    void shouldReturnAllTrainingItems() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(trainingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(List.of(testTrainingItem));

      List<TrainingItemDTO> result = trainingItemService.getTrainingItemsByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Java Certification", result.get(0).getName());
    }

    @Test
    @DisplayName("Should throw when fiscal year not found")
    void shouldThrowWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> trainingItemService.getTrainingItemsByFiscalYearId(99L, "testuser"));
    }

    @Test
    @DisplayName("Should throw when access denied")
    void shouldThrowWhenAccessDenied() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(permissionService.hasAccess(1L, "noaccess")).thenReturn(false);

      assertThrows(IllegalArgumentException.class,
          () -> trainingItemService.getTrainingItemsByFiscalYearId(1L, "noaccess"));
    }
  }

  @Nested
  @DisplayName("getTrainingItemById")
  class GetTrainingItemById {

    @Test
    @DisplayName("Should return training item by id")
    void shouldReturnTrainingItemById() {
      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));

      Optional<TrainingItemDTO> result = trainingItemService.getTrainingItemById(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("Java Certification", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
      when(trainingItemRepository.findById(99L)).thenReturn(Optional.empty());

      Optional<TrainingItemDTO> result = trainingItemService.getTrainingItemById(99L, "testuser");

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("createTrainingItem")
  class CreateTrainingItem {

    @Test
    @DisplayName("Should create training item successfully")
    void shouldCreateTrainingItem() {
      TrainingItemDTO dto = new TrainingItemDTO();
      dto.setName("Java Certification");
      dto.setFiscalYearId(1L);
      dto.setStatus("PLANNED");
      dto.setTrainingType("COURSE_TRAINING");
      dto.setFormat("IN_PERSON");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(trainingItemRepository.existsByNameAndFiscalYearId("Java Certification", 1L)).thenReturn(false);
      when(trainingItemRepository.save(any(TrainingItem.class))).thenReturn(testTrainingItem);

      TrainingItemDTO result = trainingItemService.createTrainingItem(dto, "testuser");

      assertNotNull(result);
      assertEquals("Java Certification", result.getName());
      verify(trainingItemRepository).save(any(TrainingItem.class));
    }

    @Test
    @DisplayName("Should throw for duplicate name")
    void shouldThrowForDuplicateName() {
      TrainingItemDTO dto = new TrainingItemDTO();
      dto.setName("Java Certification");
      dto.setFiscalYearId(1L);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(trainingItemRepository.existsByNameAndFiscalYearId("Java Certification", 1L)).thenReturn(true);

      assertThrows(IllegalArgumentException.class,
          () -> trainingItemService.createTrainingItem(dto, "testuser"));
    }
  }

  @Nested
  @DisplayName("updateTrainingItem")
  class UpdateTrainingItem {

    @Test
    @DisplayName("Should update training item successfully")
    void shouldUpdateTrainingItem() {
      TrainingItemDTO dto = new TrainingItemDTO();
      dto.setName("Updated Certification");

      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));
      when(trainingItemRepository.existsByNameAndFiscalYearId("Updated Certification", 1L)).thenReturn(false);
      when(trainingItemRepository.save(any(TrainingItem.class))).thenReturn(testTrainingItem);

      TrainingItemDTO result = trainingItemService.updateTrainingItem(1L, dto, "testuser");

      assertNotNull(result);
      verify(trainingItemRepository, atLeastOnce()).save(any(TrainingItem.class));
    }

    @Test
    @DisplayName("Should throw when item not found")
    void shouldThrowWhenNotFound() {
      TrainingItemDTO dto = new TrainingItemDTO();
      dto.setName("Updated");

      when(trainingItemRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> trainingItemService.updateTrainingItem(99L, dto, "testuser"));
    }
  }

  @Nested
  @DisplayName("deleteTrainingItem")
  class DeleteTrainingItem {

    @Test
    @DisplayName("Should delete training item successfully")
    void shouldDeleteTrainingItem() {
      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));

      trainingItemService.deleteTrainingItem(1L, "testuser");

      verify(trainingItemRepository).delete(testTrainingItem);
    }

    @Test
    @DisplayName("Should throw when item not found")
    void shouldThrowWhenNotFound() {
      when(trainingItemRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> trainingItemService.deleteTrainingItem(99L, "testuser"));
    }

    @Test
    @DisplayName("Should throw when write access denied")
    void shouldThrowWhenWriteAccessDenied() {
      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));
      when(permissionService.hasWriteAccess(1L, "readonly")).thenReturn(false);

      assertThrows(IllegalArgumentException.class,
          () -> trainingItemService.deleteTrainingItem(1L, "readonly"));
    }
  }

  @Nested
  @DisplayName("updateTrainingItemStatus")
  class UpdateTrainingItemStatus {

    @Test
    @DisplayName("Should update status successfully")
    void shouldUpdateStatus() {
      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));
      when(trainingItemRepository.save(any(TrainingItem.class))).thenReturn(testTrainingItem);

      TrainingItemDTO result = trainingItemService.updateTrainingItemStatus(1L, "APPROVED", "testuser");

      assertNotNull(result);
      verify(trainingItemRepository).save(any(TrainingItem.class));
    }
  }

  @Nested
  @DisplayName("getMoneyAllocations")
  class GetMoneyAllocations {

    @Test
    @DisplayName("Should return allocations")
    void shouldReturnAllocations() {
      TrainingMoneyAllocation alloc = new TrainingMoneyAllocation(testTrainingItem, aBaseMoney, new BigDecimal("1000"));
      testTrainingItem.getMoneyAllocations().add(alloc);

      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));

      List<TrainingMoneyAllocationDTO> result = trainingItemService.getMoneyAllocations(1L, "testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
    }
  }

  @Nested
  @DisplayName("updateMoneyAllocations")
  class UpdateMoneyAllocations {

    @Test
    @DisplayName("Should update allocations successfully")
    void shouldUpdateAllocations() {
      TrainingMoneyAllocationDTO allocDto = new TrainingMoneyAllocationDTO();
      allocDto.setMoneyId(1L);
      allocDto.setOmAmount(new BigDecimal("2000.00"));

      when(trainingItemRepository.findById(1L)).thenReturn(Optional.of(testTrainingItem));
      when(trainingItemRepository.save(any(TrainingItem.class))).thenReturn(testTrainingItem);
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(aBaseMoney));

      TrainingItemDTO result = trainingItemService.updateMoneyAllocations(1L, List.of(allocDto), "testuser");

      assertNotNull(result);
      verify(trainingItemRepository, atLeast(2)).save(any(TrainingItem.class));
    }
  }
}
