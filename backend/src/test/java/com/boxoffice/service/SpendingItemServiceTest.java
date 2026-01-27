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
package com.boxoffice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boxoffice.dto.SpendingItemDTO;
import com.boxoffice.dto.SpendingMoneyAllocationDTO;
import com.boxoffice.model.Category;
import com.boxoffice.model.Currency;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.Money;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.SpendingItem;
import com.boxoffice.model.SpendingMoneyAllocation;
import com.boxoffice.model.User;
import com.boxoffice.repository.CategoryRepository;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.MoneyRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.SpendingItemRepository;
import com.boxoffice.repository.SpendingMoneyAllocationRepository;
import com.boxoffice.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
        allocationRepository
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
    gpuPurchase.setStatus(SpendingItem.Status.DRAFT);
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
      dto.setStatus("DRAFT");
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
        item.setStatus(SpendingItem.Status.DRAFT);
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
    @DisplayName("Updates DRAFT to PENDING")
    void updatesDraftToPending() {
      when(spendingItemRepository.findById(1L)).thenReturn(Optional.of(gpuPurchase));
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(rcRepository.findById(1L)).thenReturn(Optional.of(rc));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenAnswer(i -> i.getArgument(0));

      SpendingItemDTO result = spendingItemService.updateSpendingItemStatus(1L, "PENDING", "testuser");

      assertNotNull(result);
      assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("Updates DRAFT to CANCELLED")
    void updatesDraftToCancelled() {
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
    @DisplayName("Deletes APPROVED item")
    void deletesApprovedItem() {
      gpuPurchase.setStatus(SpendingItem.Status.APPROVED);
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
}
