/*
 * myRC - Travel Item Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.TravelItemDTO;
import com.myrc.dto.TravelMoneyAllocationDTO;
import com.myrc.model.*;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.TravelItemRepository;
import com.myrc.repository.TravelMoneyAllocationRepository;
import com.myrc.repository.TravelTravellerRepository;
import java.math.BigDecimal;
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
 * Unit tests for TravelItemServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TravelItemService Tests")
class TravelItemServiceTest {

  @Mock
  private TravelItemRepository travelItemRepository;

  @Mock
  private TravelMoneyAllocationRepository allocationRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private MoneyRepository moneyRepository;

  @Mock
  private RCPermissionService permissionService;

  @Mock
  private TravelTravellerRepository travellerRepository;

  private TravelItemServiceImpl travelItemService;

  private User testUser;
  private ResponsibilityCentre rc;
  private FiscalYear fy;
  private TravelItem testTravelItem;
  private Money aBaseMoney;

  @BeforeEach
  void setUp() {
    travelItemService = new TravelItemServiceImpl(
        travelItemRepository,
        allocationRepository,
        travellerRepository,
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

    testTravelItem = new TravelItem();
    testTravelItem.setId(1L);
    testTravelItem.setName("Ottawa Conference Trip");
    testTravelItem.setDescription("Annual government technology conference");
    testTravelItem.setDestination("Ottawa, ON");
    testTravelItem.setPurpose("Conference attendance");
    testTravelItem.setEmap("EMAP-2025-001");
    testTravelItem.setStatus(TravelItem.Status.PLANNED);
    testTravelItem.setTravelType(TravelItem.TravelType.DOMESTIC);
    testTravelItem.setFiscalYear(fy);
    testTravelItem.setMoneyAllocations(new ArrayList<>());

    lenient().when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(true);
    lenient().when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(true);
  }

  @Nested
  @DisplayName("getTravelItemsByFiscalYearId")
  class GetTravelItemsByFiscalYearId {

    @Test
    @DisplayName("Should return all travel items for fiscal year")
    void shouldReturnAllTravelItems() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(travelItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(List.of(testTravelItem));

      List<TravelItemDTO> result = travelItemService.getTravelItemsByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Ottawa Conference Trip", result.get(0).getName());
    }

    @Test
    @DisplayName("Should throw when fiscal year not found")
    void shouldThrowWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> travelItemService.getTravelItemsByFiscalYearId(99L, "testuser"));
    }

    @Test
    @DisplayName("Should throw when access denied")
    void shouldThrowWhenAccessDenied() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(permissionService.hasAccess(1L, "noaccess")).thenReturn(false);

      assertThrows(IllegalArgumentException.class,
          () -> travelItemService.getTravelItemsByFiscalYearId(1L, "noaccess"));
    }
  }

  @Nested
  @DisplayName("getTravelItemById")
  class GetTravelItemById {

    @Test
    @DisplayName("Should return travel item by id")
    void shouldReturnTravelItemById() {
      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));

      Optional<TravelItemDTO> result = travelItemService.getTravelItemById(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("Ottawa Conference Trip", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
      when(travelItemRepository.findById(99L)).thenReturn(Optional.empty());

      Optional<TravelItemDTO> result = travelItemService.getTravelItemById(99L, "testuser");

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("createTravelItem")
  class CreateTravelItem {

    @Test
    @DisplayName("Should create travel item successfully")
    void shouldCreateTravelItem() {
      TravelItemDTO dto = new TravelItemDTO();
      dto.setName("Ottawa Conference Trip");
      dto.setFiscalYearId(1L);
      dto.setStatus("PLANNED");
      dto.setTravelType("DOMESTIC");
      dto.setEmap("EMAP-2025-001");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(travelItemRepository.existsByNameAndFiscalYearId("Ottawa Conference Trip", 1L)).thenReturn(false);
      when(travelItemRepository.save(any(TravelItem.class))).thenReturn(testTravelItem);

      TravelItemDTO result = travelItemService.createTravelItem(dto, "testuser");

      assertNotNull(result);
      assertEquals("Ottawa Conference Trip", result.getName());
      verify(travelItemRepository).save(any(TravelItem.class));
    }

    @Test
    @DisplayName("Should throw for duplicate name")
    void shouldThrowForDuplicateName() {
      TravelItemDTO dto = new TravelItemDTO();
      dto.setName("Ottawa Conference Trip");
      dto.setFiscalYearId(1L);

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(travelItemRepository.existsByNameAndFiscalYearId("Ottawa Conference Trip", 1L)).thenReturn(true);

      assertThrows(IllegalArgumentException.class,
          () -> travelItemService.createTravelItem(dto, "testuser"));
    }
  }

  @Nested
  @DisplayName("updateTravelItem")
  class UpdateTravelItem {

    @Test
    @DisplayName("Should update travel item successfully")
    void shouldUpdateTravelItem() {
      TravelItemDTO dto = new TravelItemDTO();
      dto.setName("Updated Trip");

      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));
      when(travelItemRepository.existsByNameAndFiscalYearId("Updated Trip", 1L)).thenReturn(false);
      when(travelItemRepository.save(any(TravelItem.class))).thenReturn(testTravelItem);

      TravelItemDTO result = travelItemService.updateTravelItem(1L, dto, "testuser");

      assertNotNull(result);
      verify(travelItemRepository, atLeastOnce()).save(any(TravelItem.class));
    }

    @Test
    @DisplayName("Should throw when item not found")
    void shouldThrowWhenNotFound() {
      TravelItemDTO dto = new TravelItemDTO();
      dto.setName("Updated");

      when(travelItemRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> travelItemService.updateTravelItem(99L, dto, "testuser"));
    }
  }

  @Nested
  @DisplayName("deleteTravelItem")
  class DeleteTravelItem {

    @Test
    @DisplayName("Should delete travel item successfully")
    void shouldDeleteTravelItem() {
      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));

      travelItemService.deleteTravelItem(1L, "testuser");

      verify(travelItemRepository).delete(testTravelItem);
    }

    @Test
    @DisplayName("Should throw when item not found")
    void shouldThrowWhenNotFound() {
      when(travelItemRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class,
          () -> travelItemService.deleteTravelItem(99L, "testuser"));
    }

    @Test
    @DisplayName("Should throw when write access denied")
    void shouldThrowWhenWriteAccessDenied() {
      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));
      when(permissionService.hasWriteAccess(1L, "readonly")).thenReturn(false);

      assertThrows(IllegalArgumentException.class,
          () -> travelItemService.deleteTravelItem(1L, "readonly"));
    }
  }

  @Nested
  @DisplayName("updateTravelItemStatus")
  class UpdateTravelItemStatus {

    @Test
    @DisplayName("Should update status successfully")
    void shouldUpdateStatus() {
      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));
      when(travelItemRepository.save(any(TravelItem.class))).thenReturn(testTravelItem);

      TravelItemDTO result = travelItemService.updateTravelItemStatus(1L, "APPROVED", "testuser");

      assertNotNull(result);
      verify(travelItemRepository).save(any(TravelItem.class));
    }
  }

  @Nested
  @DisplayName("getMoneyAllocations")
  class GetMoneyAllocations {

    @Test
    @DisplayName("Should return allocations")
    void shouldReturnAllocations() {
      TravelMoneyAllocation alloc = new TravelMoneyAllocation(testTravelItem, aBaseMoney, new BigDecimal("1500"));
      testTravelItem.getMoneyAllocations().add(alloc);

      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));

      List<TravelMoneyAllocationDTO> result = travelItemService.getMoneyAllocations(1L, "testuser");

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
      TravelMoneyAllocationDTO allocDto = new TravelMoneyAllocationDTO();
      allocDto.setMoneyId(1L);
      allocDto.setOmAmount(new BigDecimal("3000.00"));

      when(travelItemRepository.findById(1L)).thenReturn(Optional.of(testTravelItem));
      when(travelItemRepository.save(any(TravelItem.class))).thenReturn(testTravelItem);
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(aBaseMoney));

      TravelItemDTO result = travelItemService.updateMoneyAllocations(1L, List.of(allocDto), "testuser");

      assertNotNull(result);
      verify(travelItemRepository, atLeast(2)).save(any(TravelItem.class));
    }
  }
}
