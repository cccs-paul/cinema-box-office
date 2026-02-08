/*
 * myRC - Fiscal Year Clone Service Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myrc.model.Category;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingItem;
import com.myrc.model.Money;
import com.myrc.model.MoneyAllocation;
import com.myrc.model.ProcurementEvent;
import com.myrc.model.ProcurementEventFile;
import com.myrc.model.ProcurementItem;
import com.myrc.model.ProcurementQuote;
import com.myrc.model.ProcurementQuoteFile;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingCategory;
import com.myrc.model.SpendingEvent;
import com.myrc.model.SpendingItem;
import com.myrc.model.SpendingMoneyAllocation;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.FundingItemRepository;
import com.myrc.repository.MoneyAllocationRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.ProcurementEventFileRepository;
import com.myrc.repository.ProcurementEventRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.SpendingCategoryRepository;
import com.myrc.repository.SpendingEventRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.SpendingMoneyAllocationRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for FiscalYearCloneServiceImpl.
 * Verifies deep-clone logic for fiscal years and all child entities.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FiscalYearCloneService Tests")
class FiscalYearCloneServiceImplTest {

  @Mock
  private FiscalYearRepository fiscalYearRepository;

  @Mock
  private MoneyRepository moneyRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private SpendingCategoryRepository spendingCategoryRepository;

  @Mock
  private FundingItemRepository fundingItemRepository;

  @Mock
  private MoneyAllocationRepository moneyAllocationRepository;

  @Mock
  private SpendingItemRepository spendingItemRepository;

  @Mock
  private SpendingMoneyAllocationRepository spendingMoneyAllocationRepository;

  @Mock
  private SpendingEventRepository spendingEventRepository;

  @Mock
  private ProcurementItemRepository procurementItemRepository;

  @Mock
  private ProcurementQuoteRepository procurementQuoteRepository;

  @Mock
  private ProcurementQuoteFileRepository procurementQuoteFileRepository;

  @Mock
  private ProcurementEventRepository procurementEventRepository;

  @Mock
  private ProcurementEventFileRepository procurementEventFileRepository;

  private FiscalYearCloneServiceImpl cloneService;

  private User testUser;
  private ResponsibilityCentre testRC;
  private FiscalYear sourceFY;

  @BeforeEach
  void setUp() {
    cloneService = new FiscalYearCloneServiceImpl(
        fiscalYearRepository,
        moneyRepository,
        categoryRepository,
        spendingCategoryRepository,
        fundingItemRepository,
        moneyAllocationRepository,
        spendingItemRepository,
        spendingMoneyAllocationRepository,
        spendingEventRepository,
        procurementItemRepository,
        procurementQuoteRepository,
        procurementQuoteFileRepository,
        procurementEventRepository,
        procurementEventFileRepository
    );

    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    testRC = new ResponsibilityCentre();
    testRC.setId(1L);
    testRC.setName("Test RC");
    testRC.setOwner(testUser);

    sourceFY = new FiscalYear("Source FY", "Source description", testRC);
    sourceFY.setId(10L);
    sourceFY.setShowSearchBox(true);
    sourceFY.setShowCategoryFilter(false);
    sourceFY.setGroupByCategory(true);
    sourceFY.setOnTargetMin(-5);
    sourceFY.setOnTargetMax(10);
    sourceFY.setActive(true);
  }

  @Test
  @DisplayName("Should create service successfully")
  void testServiceCreation() {
    assertNotNull(cloneService);
  }

  @Nested
  @DisplayName("Deep Clone FY Shell Tests")
  class FYShellTests {

    @Test
    @DisplayName("Should clone FY shell with all settings")
    void testCloneFYShell() {
      FiscalYear savedFY = new FiscalYear("Cloned FY", "Source description", testRC);
      savedFY.setId(20L);
      savedFY.setShowSearchBox(true);
      savedFY.setShowCategoryFilter(false);
      savedFY.setGroupByCategory(true);
      savedFY.setOnTargetMin(-5);
      savedFY.setOnTargetMax(10);

      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      stubEmptyChildEntities(10L);

      FiscalYear result = cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      assertNotNull(result);
      assertEquals("Cloned FY", result.getName());
      assertEquals(20L, result.getId());
      verify(fiscalYearRepository).save(any(FiscalYear.class));
    }

    @Test
    @DisplayName("Should clone FY with empty child entities")
    void testCloneEmptyFY() {
      FiscalYear savedFY = new FiscalYear("Empty Clone", "Source description", testRC);
      savedFY.setId(30L);

      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      stubEmptyChildEntities(10L);

      FiscalYear result = cloneService.deepCloneFiscalYear(sourceFY, "Empty Clone", testRC);

      assertNotNull(result);
      verify(moneyRepository, never()).save(any(Money.class));
      verify(categoryRepository, never()).save(any(Category.class));
      verify(fundingItemRepository, never()).save(any(FundingItem.class));
      verify(spendingItemRepository, never()).save(any(SpendingItem.class));
      verify(procurementItemRepository, never()).save(any(ProcurementItem.class));
    }
  }

  @Nested
  @DisplayName("Clone Money Types Tests")
  class MoneyCloneTests {

    @Test
    @DisplayName("Should clone money types and build ID map")
    void testCloneMoneyTypes() {
      Money sourceMoney = new Money();
      sourceMoney.setId(100L);
      sourceMoney.setCode("CAP");
      sourceMoney.setName("Capital");
      sourceMoney.setIsDefault(true);
      sourceMoney.setDisplayOrder(1);
      sourceMoney.setFiscalYear(sourceFY);

      Money clonedMoney = new Money();
      clonedMoney.setId(200L);
      clonedMoney.setCode("CAP");
      clonedMoney.setName("Capital");

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(moneyRepository.findByFiscalYearId(10L)).thenReturn(List.of(sourceMoney));
      when(moneyRepository.save(any(Money.class))).thenReturn(clonedMoney);
      stubEmptyChildEntitiesExceptMoney(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(moneyRepository).save(any(Money.class));
    }
  }

  @Nested
  @DisplayName("Clone Categories Tests")
  class CategoryCloneTests {

    @Test
    @DisplayName("Should clone categories")
    void testCloneCategories() {
      Category sourceCategory = new Category();
      sourceCategory.setId(100L);
      sourceCategory.setName("Operating");
      sourceCategory.setIsDefault(true);
      sourceCategory.setDisplayOrder(1);
      sourceCategory.setFiscalYear(sourceFY);

      Category clonedCategory = new Category();
      clonedCategory.setId(200L);
      clonedCategory.setName("Operating");

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceCategory));
      when(categoryRepository.save(any(Category.class))).thenReturn(clonedCategory);
      stubEmptyChildEntitiesExceptCategory(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(categoryRepository).save(any(Category.class));
    }
  }

  @Nested
  @DisplayName("Clone Spending Categories Tests")
  class SpendingCategoryCloneTests {

    @Test
    @DisplayName("Should clone spending categories")
    void testCloneSpendingCategories() {
      SpendingCategory sourceSpendCat = new SpendingCategory(
          "Travel", "Travel expenses", sourceFY, true, 1);
      sourceSpendCat.setId(100L);

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceSpendCat));
      when(spendingCategoryRepository.save(any(SpendingCategory.class))).thenReturn(sourceSpendCat);
      stubEmptyChildEntitiesExceptSpendingCategory(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(spendingCategoryRepository).save(any(SpendingCategory.class));
    }
  }

  @Nested
  @DisplayName("Clone Procurement Items Tests")
  class ProcurementCloneTests {

    @Test
    @DisplayName("Should clone procurement items with quotes and events")
    void testCloneProcurementItemsWithChildren() {
      Category sourceCategory = new Category();
      sourceCategory.setId(100L);
      sourceCategory.setName("Operating");
      sourceCategory.setFiscalYear(sourceFY);

      Category clonedCategory = new Category();
      clonedCategory.setId(200L);
      clonedCategory.setName("Operating");

      ProcurementItem sourceItem = new ProcurementItem();
      sourceItem.setId(300L);
      sourceItem.setName("Laptop");
      sourceItem.setDescription("Work laptop");
      sourceItem.setCategory(sourceCategory);
      sourceItem.setFiscalYear(sourceFY);

      ProcurementItem clonedItem = new ProcurementItem();
      clonedItem.setId(400L);
      clonedItem.setName("Laptop");

      ProcurementQuote sourceQuote = new ProcurementQuote();
      sourceQuote.setId(500L);
      sourceQuote.setVendorName("Vendor A");
      sourceQuote.setAmount(new BigDecimal("1500.00"));
      sourceQuote.setProcurementItem(sourceItem);

      ProcurementQuote clonedQuote = new ProcurementQuote();
      clonedQuote.setId(600L);
      clonedQuote.setVendorName("Vendor A");

      ProcurementEvent sourceEvent = new ProcurementEvent();
      sourceEvent.setId(700L);
      sourceEvent.setComment("Ordered");
      sourceEvent.setProcurementItem(sourceItem);

      ProcurementEvent clonedEvent = new ProcurementEvent();
      clonedEvent.setId(800L);

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceCategory));
      when(categoryRepository.save(any(Category.class))).thenReturn(clonedCategory);
      when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(10L))
          .thenReturn(List.of(sourceItem));
      when(procurementItemRepository.save(any(ProcurementItem.class))).thenReturn(clonedItem);
      when(procurementQuoteRepository.findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(300L))
          .thenReturn(List.of(sourceQuote));
      when(procurementQuoteRepository.save(any(ProcurementQuote.class))).thenReturn(clonedQuote);
      when(procurementQuoteFileRepository.findByQuoteIdAndActiveTrueOrderByFileNameAsc(500L))
          .thenReturn(Collections.emptyList());
      when(procurementEventRepository.findByProcurementItemIdAndActiveTrue(300L))
          .thenReturn(List.of(sourceEvent));
      when(procurementEventRepository.save(any(ProcurementEvent.class))).thenReturn(clonedEvent);
      when(procurementEventFileRepository.findByEventIdAndActiveTrue(700L))
          .thenReturn(Collections.emptyList());

      stubEmptyChildEntitiesExceptCategoryAndProcurement(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(procurementItemRepository).save(any(ProcurementItem.class));
      verify(procurementQuoteRepository).save(any(ProcurementQuote.class));
      verify(procurementEventRepository).save(any(ProcurementEvent.class));
    }

    @Test
    @DisplayName("Should clone procurement quote files with binary content")
    void testCloneProcurementQuoteFiles() {
      ProcurementItem sourceItem = new ProcurementItem();
      sourceItem.setId(300L);
      sourceItem.setName("Item");
      sourceItem.setFiscalYear(sourceFY);

      ProcurementItem clonedItem = new ProcurementItem();
      clonedItem.setId(400L);

      ProcurementQuote sourceQuote = new ProcurementQuote();
      sourceQuote.setId(500L);
      sourceQuote.setVendorName("Vendor");
      sourceQuote.setProcurementItem(sourceItem);

      ProcurementQuote clonedQuote = new ProcurementQuote();
      clonedQuote.setId(600L);

      ProcurementQuoteFile sourceFile = new ProcurementQuoteFile();
      sourceFile.setId(700L);
      sourceFile.setFileName("quote.pdf");
      sourceFile.setContentType("application/pdf");
      sourceFile.setFileSize(1024L);
      sourceFile.setContent(new byte[]{1, 2, 3, 4});
      sourceFile.setQuote(sourceQuote);

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(10L))
          .thenReturn(List.of(sourceItem));
      when(procurementItemRepository.save(any(ProcurementItem.class))).thenReturn(clonedItem);
      when(procurementQuoteRepository.findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(300L))
          .thenReturn(List.of(sourceQuote));
      when(procurementQuoteRepository.save(any(ProcurementQuote.class))).thenReturn(clonedQuote);
      when(procurementQuoteFileRepository.findByQuoteIdAndActiveTrueOrderByFileNameAsc(500L))
          .thenReturn(List.of(sourceFile));
      when(procurementQuoteFileRepository.save(any(ProcurementQuoteFile.class))).thenReturn(sourceFile);
      when(procurementEventRepository.findByProcurementItemIdAndActiveTrue(300L))
          .thenReturn(Collections.emptyList());
      stubEmptyChildEntitiesExceptProcurement(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(procurementQuoteFileRepository).save(any(ProcurementQuoteFile.class));
    }
  }

  @Nested
  @DisplayName("Clone Funding Items Tests")
  class FundingCloneTests {

    @Test
    @DisplayName("Should clone funding items with money allocations")
    void testCloneFundingItemsWithAllocations() {
      Money sourceMoney = new Money();
      sourceMoney.setId(100L);
      sourceMoney.setCode("CAP");
      sourceMoney.setName("Capital");
      sourceMoney.setFiscalYear(sourceFY);

      Money clonedMoney = new Money();
      clonedMoney.setId(200L);
      clonedMoney.setCode("CAP");

      Category sourceCategory = new Category();
      sourceCategory.setId(150L);
      sourceCategory.setName("Cat");
      sourceCategory.setFiscalYear(sourceFY);

      Category clonedCategory = new Category();
      clonedCategory.setId(250L);

      FundingItem sourceItem = new FundingItem();
      sourceItem.setId(300L);
      sourceItem.setName("Grant");
      sourceItem.setDescription("Federal grant");
      sourceItem.setCategory(sourceCategory);
      sourceItem.setFiscalYear(sourceFY);

      FundingItem clonedItem = new FundingItem();
      clonedItem.setId(400L);
      clonedItem.setName("Grant");

      MoneyAllocation sourceAlloc = new MoneyAllocation();
      sourceAlloc.setId(500L);
      sourceAlloc.setFundingItem(sourceItem);
      sourceAlloc.setMoney(sourceMoney);
      sourceAlloc.setCapAmount(new BigDecimal("10000"));
      sourceAlloc.setOmAmount(new BigDecimal("5000"));

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(moneyRepository.findByFiscalYearId(10L)).thenReturn(List.of(sourceMoney));
      when(moneyRepository.save(any(Money.class))).thenReturn(clonedMoney);
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceCategory));
      when(categoryRepository.save(any(Category.class))).thenReturn(clonedCategory);
      when(fundingItemRepository.findByFiscalYearId(10L)).thenReturn(List.of(sourceItem));
      when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(clonedItem);
      when(moneyAllocationRepository.findByFundingItemId(300L)).thenReturn(List.of(sourceAlloc));
      when(moneyAllocationRepository.save(any(MoneyAllocation.class))).thenReturn(sourceAlloc);
      stubEmptyChildEntitiesExceptMoneyAndCategoryAndFunding(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(fundingItemRepository).save(any(FundingItem.class));
      verify(moneyAllocationRepository).save(any(MoneyAllocation.class));
    }
  }

  @Nested
  @DisplayName("Clone Spending Items Tests")
  class SpendingCloneTests {

    @Test
    @DisplayName("Should clone spending items with money allocations and events")
    void testCloneSpendingItemsWithChildren() {
      Money sourceMoney = new Money();
      sourceMoney.setId(100L);
      sourceMoney.setCode("OM");
      sourceMoney.setFiscalYear(sourceFY);

      Money clonedMoney = new Money();
      clonedMoney.setId(200L);
      clonedMoney.setCode("OM");

      Category sourceCategory = new Category();
      sourceCategory.setId(150L);
      sourceCategory.setName("Cat");
      sourceCategory.setFiscalYear(sourceFY);

      Category clonedCategory = new Category();
      clonedCategory.setId(250L);

      SpendingItem sourceItem = new SpendingItem();
      sourceItem.setId(300L);
      sourceItem.setName("Office supplies");
      sourceItem.setCategory(sourceCategory);
      sourceItem.setFiscalYear(sourceFY);

      SpendingItem clonedItem = new SpendingItem();
      clonedItem.setId(400L);
      clonedItem.setName("Office supplies");

      SpendingMoneyAllocation sourceAlloc = new SpendingMoneyAllocation();
      sourceAlloc.setId(500L);
      sourceAlloc.setSpendingItem(sourceItem);
      sourceAlloc.setMoney(sourceMoney);
      sourceAlloc.setCapAmount(new BigDecimal("500"));
      sourceAlloc.setOmAmount(new BigDecimal("300"));

      SpendingEvent sourceEvent = new SpendingEvent();
      sourceEvent.setId(600L);
      sourceEvent.setComment("Purchased");

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);
      when(moneyRepository.findByFiscalYearId(10L)).thenReturn(List.of(sourceMoney));
      when(moneyRepository.save(any(Money.class))).thenReturn(clonedMoney);
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceCategory));
      when(categoryRepository.save(any(Category.class))).thenReturn(clonedCategory);
      when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(10L))
          .thenReturn(List.of(sourceItem));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenReturn(clonedItem);
      when(spendingMoneyAllocationRepository.findBySpendingItemId(300L))
          .thenReturn(List.of(sourceAlloc));
      when(spendingMoneyAllocationRepository.save(any(SpendingMoneyAllocation.class)))
          .thenReturn(sourceAlloc);
      when(spendingEventRepository.findBySpendingItemIdAndActiveTrue(300L))
          .thenReturn(List.of(sourceEvent));
      when(spendingEventRepository.save(any(SpendingEvent.class))).thenReturn(sourceEvent);
      stubEmptyChildEntitiesExceptMoneyAndCategoryAndSpending(10L);

      cloneService.deepCloneFiscalYear(sourceFY, "Cloned FY", testRC);

      verify(spendingItemRepository).save(any(SpendingItem.class));
      verify(spendingMoneyAllocationRepository).save(any(SpendingMoneyAllocation.class));
      verify(spendingEventRepository).save(any(SpendingEvent.class));
    }
  }

  @Nested
  @DisplayName("Full Integration Clone Tests")
  class FullCloneTests {

    @Test
    @DisplayName("Should clone all entity types together")
    void testFullDeepClone() {
      // Money
      Money sourceMoney = new Money();
      sourceMoney.setId(100L);
      sourceMoney.setCode("CAP");
      sourceMoney.setName("Capital");
      sourceMoney.setFiscalYear(sourceFY);

      Money clonedMoney = new Money();
      clonedMoney.setId(200L);
      clonedMoney.setCode("CAP");

      // Category
      Category sourceCategory = new Category();
      sourceCategory.setId(150L);
      sourceCategory.setName("Operations");
      sourceCategory.setFiscalYear(sourceFY);

      Category clonedCategory = new Category();
      clonedCategory.setId(250L);

      // Spending Category
      SpendingCategory sourceSpendCat = new SpendingCategory(
          "Travel", "Travel expenses", sourceFY, false, 1);
      sourceSpendCat.setId(160L);

      // Funding Item
      FundingItem sourceFundingItem = new FundingItem();
      sourceFundingItem.setId(300L);
      sourceFundingItem.setName("Grant");
      sourceFundingItem.setCategory(sourceCategory);
      sourceFundingItem.setFiscalYear(sourceFY);

      FundingItem clonedFunding = new FundingItem();
      clonedFunding.setId(400L);

      // Spending Item
      SpendingItem sourceSpendingItem = new SpendingItem();
      sourceSpendingItem.setId(500L);
      sourceSpendingItem.setName("Office supplies");
      sourceSpendingItem.setCategory(sourceCategory);
      sourceSpendingItem.setFiscalYear(sourceFY);

      SpendingItem clonedSpending = new SpendingItem();
      clonedSpending.setId(600L);

      FiscalYear savedFY = createSavedFY(20L);
      when(fiscalYearRepository.save(any(FiscalYear.class))).thenReturn(savedFY);

      // Money
      when(moneyRepository.findByFiscalYearId(10L)).thenReturn(List.of(sourceMoney));
      when(moneyRepository.save(any(Money.class))).thenReturn(clonedMoney);

      // Categories
      when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceCategory));
      when(categoryRepository.save(any(Category.class))).thenReturn(clonedCategory);

      // Spending Categories
      when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(10L))
          .thenReturn(List.of(sourceSpendCat));
      when(spendingCategoryRepository.save(any(SpendingCategory.class))).thenReturn(sourceSpendCat);

      // Procurement (empty)
      when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(10L))
          .thenReturn(Collections.emptyList());

      // Funding
      when(fundingItemRepository.findByFiscalYearId(10L)).thenReturn(List.of(sourceFundingItem));
      when(fundingItemRepository.save(any(FundingItem.class))).thenReturn(clonedFunding);
      when(moneyAllocationRepository.findByFundingItemId(300L)).thenReturn(Collections.emptyList());

      // Spending
      when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(10L))
          .thenReturn(List.of(sourceSpendingItem));
      when(spendingItemRepository.save(any(SpendingItem.class))).thenReturn(clonedSpending);
      when(spendingMoneyAllocationRepository.findBySpendingItemId(500L))
          .thenReturn(Collections.emptyList());
      when(spendingEventRepository.findBySpendingItemIdAndActiveTrue(500L))
          .thenReturn(Collections.emptyList());

      FiscalYear result = cloneService.deepCloneFiscalYear(sourceFY, "Full Clone", testRC);

      assertNotNull(result);
      verify(moneyRepository, times(1)).save(any(Money.class));
      verify(categoryRepository, times(1)).save(any(Category.class));
      verify(spendingCategoryRepository, times(1)).save(any(SpendingCategory.class));
      verify(fundingItemRepository, times(1)).save(any(FundingItem.class));
      verify(spendingItemRepository, times(1)).save(any(SpendingItem.class));
    }
  }

  // =========== Helper Methods ===========

  private FiscalYear createSavedFY(Long id) {
    FiscalYear fy = new FiscalYear("Cloned FY", "Source description", testRC);
    fy.setId(id);
    return fy;
  }

  private void stubEmptyChildEntities(Long sourceFYId) {
    when(moneyRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptMoney(Long sourceFYId) {
    when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptCategory(Long sourceFYId) {
    when(moneyRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptSpendingCategory(Long sourceFYId) {
    when(moneyRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptCategoryAndProcurement(Long sourceFYId) {
    when(moneyRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptProcurement(Long sourceFYId) {
    when(moneyRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptMoneyAndCategoryAndFunding(Long sourceFYId) {
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
  }

  private void stubEmptyChildEntitiesExceptMoneyAndCategoryAndSpending(Long sourceFYId) {
    when(spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(sourceFYId))
        .thenReturn(Collections.emptyList());
    when(fundingItemRepository.findByFiscalYearId(sourceFYId)).thenReturn(Collections.emptyList());
  }
}
