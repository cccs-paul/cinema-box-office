/*
 * myRC - Spending Item Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for SpendingItemServiceImpl.
 */
package com.myrc.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.myrc.dto.SpendingItemDTO;
import com.myrc.dto.SpendingMoneyAllocationDTO;
import com.myrc.model.Category;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.Money;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingItem;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.ProcurementEventRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingEventRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.SpendingMoneyAllocationRepository;
import com.myrc.repository.UserRepository;

/**
 * Unit tests for SpendingItemServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpendingItemService Tests")
class SpendingItemServiceTest {

  @Mock
  private SpendingItemRepository spendingItemRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private ResponsibilityCentreRepository rcRepository;

  @Mock
  private RCAccessRepository accessRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MoneyRepository moneyRepository;

  @Mock
  private SpendingMoneyAllocationRepository allocationRepository;

  @Mock
  private SpendingEventRepository spendingEventRepository;

  @Mock
  private ProcurementEventRepository procurementEventRepository;

  private SpendingItemServiceImpl spendingItemService;

  private User testUser;
  private ResponsibilityCentre rc;
  private FiscalYear fy;
  private Category gpuCategory;
  private SpendingItem gpuPurchase;
  private Money aBaseMoney;
  private RCAccess rcAccess;

  @BeforeEach
  void setUp() {
    spendingItemService = new SpendingItemServiceImpl(
        spendingItemRepository,
        categoryRepository,
        fiscalYearRepository,
        rcRepository,
        accessRepository,
        userRepository,
        moneyRepository,
        allocationRepository,
        spendingEventRepository,
        procurementEventRepository
    );

    // Setup test user
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    // Setup RC
    rc = new ResponsibilityCentre();
    rc.setId(1L);
    rc.setName("Demo RC");
    rc.setOwner(testUser);

    // Setup FY
    fy = new FiscalYear();
    fy.setId(1L);
    fy.setName("FY 2025-2026");
    fy.setResponsibilityCentre(rc);

    // Setup Category
    gpuCategory = new Category();
    gpuCategory.setId(2L);
    gpuCategory.setName("GPUs");
    gpuCategory.setDescription("Graphics Processing Units");
    gpuCategory.setIsDefault(true);
    gpuCategory.setFiscalYear(fy);

    // Setup Money
    aBaseMoney = new Money();
    aBaseMoney.setId(1L);
    aBaseMoney.setCode("AB");
    aBaseMoney.setName("A-Base");
    aBaseMoney.setIsDefault(true);
    aBaseMoney.setFiscalYear(fy);

    // Setup SpendingItem
    gpuPurchase = new SpendingItem();
    gpuPurchase.setId(1L);
    gpuPurchase.setName("GPU Purchase");
    gpuPurchase.setDescription("NVIDIA A100 GPUs");
    gpuPurchase.setVendor("NVIDIA");
    gpuPurchase.setReferenceNumber("PO-001");
    gpuPurchase.setAmount(new BigDecimal("50000"));
    gpuPurchase.setStatus(SpendingItem.Status.PLANNING);
    gpuPurchase.setCurrency(Currency.CAD);
    gpuPurchase.setCategory(gpuCategory);
    gpuPurchase.setFiscalYear(fy);
    gpuPurchase.setMoneyAllocations(new ArrayList<>());

    // Setup RCAccess
    rcAccess = new RCAccess();
    rcAccess.setId(1L);
    rcAccess.setUser(testUser);
    rcAccess.setResponsibilityCentre(rc);
    rcAccess.setAccessLevel(RCAccess.AccessLevel.READ_WRITE);

    // Lenient mocking for event tracking enrichment (called by enrichEventTrackingInfo)
    org.mockito.Mockito.lenient()
        .when(spendingEventRepository.countBySpendingItemIdAndActiveTrue(anyLong()))
        .thenReturn(0L);
    org.mockito.Mockito.lenient()
        .when(spendingEventRepository.findMostRecentBySpendingItemId(anyLong()))
        .thenReturn(Optional.empty());
    org.mockito.Mockito.lenient()
        .when(procurementEventRepository.findMostRecentByProcurementItemId(anyLong()))
        .thenReturn(Optional.empty());
  }

  @Nested
  @DisplayName("getSpendingItemsByFiscalYearId Tests")
  class GetSpendingItemsByFiscalYearIdTests {

    @Test
    @DisplayName("Returns items for owner")
    void returnsItemsForOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(Arrays.asList(gpuPurchase));

      List<SpendingItemDTO> result = spendingItemService.getSpendingItemsByFiscalYearId(1L, "testuser");

      assertEquals(1, result.size());
      assertEquals("GPU Purchase", result.get(0).getName());
    }

    @Test
    @DisplayName("Returns items for user with access")
    void returnsItemsForUserWithAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(accessRepository.findByResponsibilityCentreAndUser(rc, otherUser))
          .thenReturn(Optional.of(rcAccess));
      when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(Arrays.asList(gpuPurchase));

      List<SpendingItemDTO> result = spendingItemService.getSpendingItemsByFiscalYearId(1L, "otheruser");

      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Throws exception when no access")
    void throwsExceptionWhenNoAccess() {
      User otherUser = new User();
      otherUser.setId(2L);
      otherUser.setUsername("otheruser");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(accessRepository.findByResponsibilityCentreAndUser(rc, otherUser))
          .thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          spendingItemService.getSpendingItemsByFiscalYearId(1L, "otheruser"));
    }

    @Test
    @DisplayName("Throws exception when FY not found")
    void throwsExceptionWhenFyNotFound() {
      when(fiscalYearRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          spendingItemService.getSpendingItemsByFiscalYearId(99L, "testuser"));
    }
  }

  @Nested
  @DisplayName("getSpendingItemById Tests")
  class GetSpendingItemByIdTests {

    @Test
    @DisplayName("Returns item when found")
    void returnsItemWhenFound() {
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));

      Optional<SpendingItemDTO> result = spendingItemService.getSpendingItemById(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("GPU Purchase", result.get().getName());
    }

    @Test
    @DisplayName("Returns empty when not found")
    void returnsEmptyWhenNotFound() {
      when(spendingItemRepository.findById(99L)).thenReturn(Optional.empty());

      Optional<SpendingItemDTO> result = spendingItemService.getSpendingItemById(99L, "testuser");

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("createSpendingItem Tests")
  class CreateSpendingItemTests {

    @Test
    @DisplayName("Creates item successfully")
    void createsItemSuccessfully() {
      SpendingItemDTO dto = new SpendingItemDTO();
      dto.setFiscalYearId(1L);
      dto.setName("New Purchase");
      dto.setDescription("New description");
      dto.setCategoryId(2L);
      dto.setStatus("PLANNING");
      dto.setCurrency("CAD");

      SpendingMoneyAllocationDTO allocationDTO = new SpendingMoneyAllocationDTO();
      allocationDTO.setMoneyId(1L);
      allocationDTO.setCapAmount(new BigDecimal("10000"));
      allocationDTO.setOmAmount(BigDecimal.ZERO);
      dto.setMoneyAllocations(Arrays.asList(allocationDTO));

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(categoryRepository.findById(2L)).thenReturn(Optional.of(gpuCategory));
      when(moneyRepository.findByFiscalYearId(1L)).thenReturn(Arrays.asList(aBaseMoney));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenAnswer(i -> {
        SpendingItem item = i.getArgument(0);
        item.setId(2L);
        return item;
      });
      when(spendingItemRepository.findById(2L)).thenAnswer(i -> {
        SpendingItem item = new SpendingItem();
        item.setId(2L);
        item.setName("New Purchase");
        item.setFiscalYear(fy);
        item.setCategory(gpuCategory);
        item.setStatus(SpendingItem.Status.PLANNING);
        item.setCurrency(Currency.CAD);
        return Optional.of(item);
      });

      SpendingItemDTO result = spendingItemService.createSpendingItem(dto, "testuser");

      assertNotNull(result);
      assertEquals("New Purchase", result.getName());
      // save is called twice: once to create, once after adding money allocations
      verify(spendingItemRepository, org.mockito.Mockito.times(2)).save(any(SpendingItem.class));
    }

    @Test
    @DisplayName("Throws exception on empty name")
    void throwsExceptionOnEmptyName() {
      SpendingItemDTO dto = new SpendingItemDTO();
      dto.setFiscalYearId(1L);
      dto.setName("");

      // Name validation happens before any repository calls
      assertThrows(IllegalArgumentException.class, () ->
          spendingItemService.createSpendingItem(dto, "testuser"));
    }
  }

  @Nested
  @DisplayName("updateSpendingItemStatus Tests")
  class UpdateSpendingItemStatusTests {

    @Test
    @DisplayName("Updates PLANNING to COMMITTED")
    void updatesPlanningToCommitted() {
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenAnswer(i -> i.getArgument(0));

      SpendingItemDTO result = spendingItemService.updateSpendingItemStatus(1L, "COMMITTED", "testuser");

      assertNotNull(result);
      assertEquals("COMMITTED", result.getStatus());
    }

    @Test
    @DisplayName("Updates PLANNING to CANCELLED")
    void updatesPlanningToCancelled() {
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenAnswer(i -> i.getArgument(0));

      SpendingItemDTO result = spendingItemService.updateSpendingItemStatus(1L, "CANCELLED", "testuser");

      assertNotNull(result);
      assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    @DisplayName("Throws exception on invalid status")
    void throwsExceptionOnInvalidStatus() {
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));

      assertThrows(IllegalArgumentException.class, () ->
          spendingItemService.updateSpendingItemStatus(1L, "INVALID", "testuser"));
    }
  }

  @Nested
  @DisplayName("deleteSpendingItem Tests")
  class DeleteSpendingItemTests {

    @Test
    @DisplayName("Deletes DRAFT item")
    void deletesDraftItem() {
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));

      spendingItemService.deleteSpendingItem(1L, "testuser");

      verify(spendingItemRepository).delete(gpuPurchase);
    }

    @Test
    @DisplayName("Deletes CANCELLED item")
    void deletesCancelledItem() {
      gpuPurchase.setStatus(SpendingItem.Status.CANCELLED);
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));

      spendingItemService.deleteSpendingItem(1L, "testuser");

      verify(spendingItemRepository).delete(gpuPurchase);
    }

    @Test
    @DisplayName("Deletes COMMITTED item")
    void deletesApprovedItem() {
      gpuPurchase.setStatus(SpendingItem.Status.COMMITTED);
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));

      spendingItemService.deleteSpendingItem(1L, "testuser");

      verify(spendingItemRepository).delete(gpuPurchase);
    }

    @Test
    @DisplayName("Throws exception when not found")
    void throwsExceptionWhenNotFound() {
      when(spendingItemRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          spendingItemService.deleteSpendingItem(99L, "testuser"));
    }
  }

  @Nested
  @DisplayName("updateMoneyAllocations Tests")
  class UpdateMoneyAllocationsTests {

    @Test
    @DisplayName("Updates allocations successfully")
    void updatesAllocationsSuccessfully() {
      SpendingMoneyAllocationDTO allocationDTO = new SpendingMoneyAllocationDTO();
      allocationDTO.setMoneyId(1L);
      allocationDTO.setCapAmount(new BigDecimal("30000"));
      allocationDTO.setOmAmount(new BigDecimal("10000"));

      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(moneyRepository.findById(1L)).thenReturn(Optional.of(aBaseMoney));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenAnswer(i -> i.getArgument(0));

      SpendingItemDTO result = spendingItemService.updateMoneyAllocations(
          1L, Arrays.asList(allocationDTO), "testuser");

      assertNotNull(result);
      verify(spendingItemRepository).save(any(SpendingItem.class));
    }

    @Test
    @DisplayName("Throws exception when no valid allocation")
    void throwsExceptionWhenNoValidAllocation() {
      SpendingMoneyAllocationDTO zeroAllocation = new SpendingMoneyAllocationDTO();
      zeroAllocation.setMoneyId(1L);
      zeroAllocation.setCapAmount(BigDecimal.ZERO);
      zeroAllocation.setOmAmount(BigDecimal.ZERO);

      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      // Note: moneyRepository.findById is not called because validation fails before processing allocations

      assertThrows(IllegalArgumentException.class, () ->
          spendingItemService.updateMoneyAllocations(1L, Arrays.asList(zeroAllocation), "testuser"));
    }
  }

  @Nested
  @DisplayName("Event Tracking Enrichment Tests")
  class EventTrackingEnrichmentTests {

    @Test
    @DisplayName("Populates event count and most recent event for standalone items")
    void populatesEventTrackingForStandaloneItems() {
      // Setup: gpuPurchase has no procurementItem (standalone)
      com.myrc.model.SpendingEvent mockEvent = new com.myrc.model.SpendingEvent();
      mockEvent.setId(10L);
      mockEvent.setSpendingItem(gpuPurchase);
      mockEvent.setEventType(com.myrc.model.SpendingEvent.EventType.SECTION_32_PROVIDED);
      mockEvent.setEventDate(LocalDate.of(2025, 12, 1));
      mockEvent.setCreatedBy("admin");

      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(Arrays.asList(gpuPurchase));
      when(spendingEventRepository.countBySpendingItemIdAndActiveTrue(1L)).thenReturn(3L);
      when(spendingEventRepository.findMostRecentBySpendingItemId(1L))
          .thenReturn(Optional.of(mockEvent));

      List<SpendingItemDTO> result = spendingItemService.getSpendingItemsByFiscalYearId(1L, "testuser");

      assertEquals(1, result.size());
      SpendingItemDTO dto = result.get(0);
      assertEquals(3, dto.getEventCount());
      assertEquals("SECTION_32_PROVIDED", dto.getMostRecentEventType());
      assertEquals("2025-12-01", dto.getMostRecentEventDate());
    }

    @Test
    @DisplayName("Event count is zero for items with no events")
    void eventCountZeroForNoEvents() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(fy));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(Arrays.asList(gpuPurchase));
      when(spendingEventRepository.countBySpendingItemIdAndActiveTrue(1L)).thenReturn(0L);

      List<SpendingItemDTO> result = spendingItemService.getSpendingItemsByFiscalYearId(1L, "testuser");

      assertEquals(1, result.size());
      assertEquals(0, result.get(0).getEventCount());
      // mostRecentEventType should remain null when no events
      assertEquals(null, result.get(0).getMostRecentEventType());
    }
  }
}
