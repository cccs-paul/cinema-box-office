/*
 * myRC - Funding Item Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-17
 * Version: 1.0.0
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.myrc.dto.FundingItemDTO;
import com.myrc.dto.MoneyAllocationDTO;
import com.myrc.model.Category;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingItem;
import com.myrc.model.FundingSource;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.FundingItemRepository;
import com.myrc.repository.MoneyAllocationRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * Unit tests for FundingItemServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-17
 */
@ExtendWith(MockitoExtension.class)
class FundingItemServiceTest {

  @Mock
  private FundingItemRepository fundingItemRepository;

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
  private MoneyAllocationRepository moneyAllocationRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private RCPermissionService permissionService;

  private FundingItemServiceImpl fundingItemService;
  private User testUser;
  private ResponsibilityCentre testRC;
  private FiscalYear testFY;
  private Category testCategory;
  private FundingItem testFundingItem;
  private List<MoneyAllocationDTO> validMoneyAllocations;

  @BeforeEach
  void setUp() {
    fundingItemService = new FundingItemServiceImpl(
        fundingItemRepository,
        fiscalYearRepository,
        rcRepository,
        accessRepository,
        userRepository,
        moneyRepository,
        moneyAllocationRepository,
        categoryRepository,
        permissionService
    );

    // Set up valid money allocations (required for creating funding items)
    MoneyAllocationDTO allocation = new MoneyAllocationDTO();
    allocation.setMoneyId(1L);
    allocation.setCapAmount(new BigDecimal("100.00"));
    allocation.setOmAmount(new BigDecimal("50.00"));
    validMoneyAllocations = Collections.singletonList(allocation);

    // Set up test user
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setFullName("Test User");

    // Set up test RC
    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(testUser);

    // Set up test fiscal year
    testFY = new FiscalYear();
    testFY.setId(1L);
    testFY.setName("FY 2026-2027");
    testFY.setResponsibilityCentre(testRC);

    // Set up test category
    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Test Category");
    testCategory.setDescription("Test category description");

    // Set up test funding item
    testFundingItem = new FundingItem();
    testFundingItem.setId(1L);
    testFundingItem.setName("Test Funding Item");
    testFundingItem.setDescription("Test Description");
    testFundingItem.setFiscalYear(testFY);
    testFundingItem.setCategory(testCategory);
    testFundingItem.setSource(FundingSource.BUSINESS_PLAN);
    testFundingItem.setCurrency(Currency.CAD);
    testFundingItem.setExchangeRate(BigDecimal.ONE);

    org.mockito.Mockito.lenient()
        .when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(true);
    org.mockito.Mockito.lenient()
        .when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(true);
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(fundingItemService);
  }

  @Nested
  @DisplayName("getFundingItemsByFiscalYearId Tests")
  class GetFundingItemsTests {

    @Test
    @DisplayName("Should return funding items for fiscal year when user is owner")
    void shouldReturnFundingItemsForFiscalYearWhenOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(fundingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(Arrays.asList(testFundingItem));

      List<FundingItemDTO> result = fundingItemService.getFundingItemsByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Test Funding Item", result.get(0).getName());
    }

    @Test
    @DisplayName("Should throw exception when fiscal year not found")
    void shouldThrowExceptionWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.getFundingItemsByFiscalYearId(999L, "testuser"));
    }

    @Test
    @DisplayName("Should return empty list when no funding items exist")
    void shouldReturnEmptyListWhenNoItems() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(fundingItemRepository.findByFiscalYearIdOrderByNameAsc(1L))
          .thenReturn(Arrays.asList());

      List<FundingItemDTO> result = fundingItemService.getFundingItemsByFiscalYearId(1L, "testuser");

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when user has no access")
    void shouldThrowExceptionWhenNoAccess() {
      when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(false);
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.getFundingItemsByFiscalYearId(1L, "otheruser"));
    }
  }

  @Nested
  @DisplayName("getFundingItemById Tests")
  class GetFundingItemByIdTests {

    @Test
    @DisplayName("Should return funding item by ID when user is owner")
    void shouldReturnFundingItemByIdWhenOwner() {
      when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));

      Optional<FundingItemDTO> result = fundingItemService.getFundingItemById(1L, "testuser");

      assertTrue(result.isPresent());
      assertEquals("Test Funding Item", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when funding item not found")
    void shouldReturnEmptyWhenNotFound() {
      when(fundingItemRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<FundingItemDTO> result = fundingItemService.getFundingItemById(999L, "testuser");

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when user has no access")
    void shouldReturnEmptyWhenNoAccess() {
      when(permissionService.hasAccess(anyLong(), anyString())).thenReturn(false);
      when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));

      Optional<FundingItemDTO> result = fundingItemService.getFundingItemById(1L, "otheruser");

      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("createFundingItem Tests")
  class CreateFundingItemTests {

    @Test
    @DisplayName("Should create funding item successfully when user is owner")
    void shouldCreateFundingItemSuccessfullyWhenOwner() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(fundingItemRepository.existsByNameAndFiscalYear("New Funding Item", testFY)).thenReturn(false);
      when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(testFundingItem);

      FundingItemDTO result = fundingItemService.createFundingItem(
          1L, "testuser", "New Funding Item", "Description",
          "BUSINESS_PLAN", "Comments", "CAD", BigDecimal.ONE, null, validMoneyAllocations);

      assertNotNull(result);
      // save() is called twice: once for item creation and once for money allocations
      verify(fundingItemRepository, atLeast(1)).save(any(FundingItem.class));
    }

    @Test
    @DisplayName("Should throw exception when fiscal year not found")
    void shouldThrowExceptionWhenFiscalYearNotFound() {
      when(fiscalYearRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.createFundingItem(
              999L, "testuser", "New Funding Item", "Description",
              "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations));
    }

    @Test
    @DisplayName("Should throw exception when duplicate name")
    void shouldThrowExceptionWhenDuplicateName() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(fundingItemRepository.existsByNameAndFiscalYear("Test Funding Item", testFY)).thenReturn(true);

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.createFundingItem(
              1L, "testuser", "Test Funding Item", "Description",
              "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(false);
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.createFundingItem(
              1L, "otheruser", "New Funding Item", "Description",
              "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations));
    }

    @Test
    @DisplayName("Should create with category when provided")
    void shouldCreateWithCategory() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(fundingItemRepository.existsByNameAndFiscalYear("New Funding Item", testFY)).thenReturn(false);
      testCategory.setFiscalYear(testFY); // Category must belong to the same fiscal year
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(testFundingItem);

      FundingItemDTO result = fundingItemService.createFundingItem(
          1L, "testuser", "New Funding Item", "Description",
          "BUSINESS_PLAN", "Comments", "CAD", BigDecimal.ONE, 1L, validMoneyAllocations);

      assertNotNull(result);
      verify(categoryRepository).findById(1L);
    }
  }

  @Nested
  @DisplayName("updateFundingItem Tests")
  class UpdateFundingItemTests {

    @Test
    @DisplayName("Should update funding item successfully when user is owner")
    void shouldUpdateFundingItemSuccessfullyWhenOwner() {
      testCategory.setFiscalYear(testFY); // Category must belong to the same fiscal year
      when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
      when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(testFundingItem);

      Optional<FundingItemDTO> result = fundingItemService.updateFundingItem(
          1L, "testuser", "Updated Name", "Updated Description",
          "ON_RAMP", "Comments", "CAD", BigDecimal.ONE, 1L, null);

      assertTrue(result.isPresent());
      verify(fundingItemRepository).save(any(FundingItem.class));
    }

    @Test
    @DisplayName("Should return empty when funding item not found")
    void shouldReturnEmptyWhenNotFound() {
      when(fundingItemRepository.findById(999L)).thenReturn(Optional.empty());

      Optional<FundingItemDTO> result = fundingItemService.updateFundingItem(
          999L, "testuser", "Updated Name", "Description",
          "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations);

      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(false);
      when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.updateFundingItem(
              1L, "otheruser", "Updated Name", "Description",
              "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations));
    }
  }

  @Nested
  @DisplayName("deleteFundingItem Tests")
  class DeleteFundingItemTests {

    @Test
    @DisplayName("Should delete funding item successfully when user is owner")
    void shouldDeleteFundingItemSuccessfullyWhenOwner() {
      when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));
      doNothing().when(fundingItemRepository).delete(testFundingItem);

      assertDoesNotThrow(() -> fundingItemService.deleteFundingItem(1L, "testuser"));
      verify(fundingItemRepository).delete(testFundingItem);
    }

    @Test
    @DisplayName("Should throw exception when funding item not found")
    void shouldThrowExceptionWhenNotFound() {
      when(fundingItemRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.deleteFundingItem(999L, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when user has no write access")
    void shouldThrowExceptionWhenNoWriteAccess() {
      when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(false);
      when(fundingItemRepository.findById(1L)).thenReturn(Optional.of(testFundingItem));

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.deleteFundingItem(1L, "otheruser"));
    }
  }

  @Nested
  @DisplayName("Access Control Tests")
  class AccessControlTests {

    @Test
    @DisplayName("User with READ_WRITE access should be able to create funding item")
    void userWithReadWriteAccessShouldCreate() {
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));
      when(fundingItemRepository.existsByNameAndFiscalYear("New Item", testFY)).thenReturn(false);
      when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(testFundingItem);

      FundingItemDTO result = fundingItemService.createFundingItem(
          1L, "accessuser", "New Item", "Description",
          "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations);

      assertNotNull(result);
    }

    @Test
    @DisplayName("User with READ_ONLY access should not be able to create funding item")
    void userWithReadOnlyAccessShouldNotCreate() {
      when(permissionService.hasWriteAccess(anyLong(), anyString())).thenReturn(false);
      when(fiscalYearRepository.findById(1L)).thenReturn(Optional.of(testFY));

      assertThrows(IllegalArgumentException.class, () ->
          fundingItemService.createFundingItem(
              1L, "accessuser", "New Item", "Description",
              "BUSINESS_PLAN", null, "CAD", null, null, validMoneyAllocations));
    }
  }
}
