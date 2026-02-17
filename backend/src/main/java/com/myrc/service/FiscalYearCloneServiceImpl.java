/*
 * myRC - Fiscal Year Clone Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

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
import com.myrc.model.TrainingItem;
import com.myrc.model.TrainingMoneyAllocation;
import com.myrc.model.TravelItem;
import com.myrc.model.TravelMoneyAllocation;
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
import com.myrc.repository.TrainingItemRepository;
import com.myrc.repository.TrainingMoneyAllocationRepository;
import com.myrc.repository.TravelItemRepository;
import com.myrc.repository.TravelMoneyAllocationRepository;
import com.myrc.service.AuditService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of FiscalYearCloneService.
 * Performs deep cloning of a fiscal year and all its child entities.
 *
 * <p>Clone order respects referential integrity:</p>
 * <ol>
 *   <li>FiscalYear (shell)</li>
 *   <li>Money types (builds old→new Money ID map)</li>
 *   <li>Categories (builds old→new Category ID map)</li>
 *   <li>Spending Categories</li>
 *   <li>Procurement Items + Quotes + Quote Files + Events + Event Files
 *       (builds old→new ProcurementItem ID map)</li>
 *   <li>Funding Items + Money Allocations (remaps Money IDs)</li>
 *   <li>Spending Items + Money Allocations + Spending Events
 *       (remaps Money IDs, Category IDs, ProcurementItem IDs)</li>
 * </ol>
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-10
 */
@Service
@Transactional
public class FiscalYearCloneServiceImpl implements FiscalYearCloneService {

  private static final Logger logger = Logger.getLogger(FiscalYearCloneServiceImpl.class.getName());

  private final FiscalYearRepository fiscalYearRepository;
  private final MoneyRepository moneyRepository;
  private final CategoryRepository categoryRepository;
  private final SpendingCategoryRepository spendingCategoryRepository;
  private final FundingItemRepository fundingItemRepository;
  private final MoneyAllocationRepository moneyAllocationRepository;
  private final SpendingItemRepository spendingItemRepository;
  private final SpendingMoneyAllocationRepository spendingMoneyAllocationRepository;
  private final SpendingEventRepository spendingEventRepository;
  private final ProcurementItemRepository procurementItemRepository;
  private final ProcurementQuoteRepository procurementQuoteRepository;
  private final ProcurementQuoteFileRepository procurementQuoteFileRepository;
  private final ProcurementEventRepository procurementEventRepository;
  private final ProcurementEventFileRepository procurementEventFileRepository;
  private final TrainingItemRepository trainingItemRepository;
  private final TrainingMoneyAllocationRepository trainingMoneyAllocationRepository;
  private final TravelItemRepository travelItemRepository;
  private final TravelMoneyAllocationRepository travelMoneyAllocationRepository;
  private final AuditService auditService;

  public FiscalYearCloneServiceImpl(
      FiscalYearRepository fiscalYearRepository,
      MoneyRepository moneyRepository,
      CategoryRepository categoryRepository,
      SpendingCategoryRepository spendingCategoryRepository,
      FundingItemRepository fundingItemRepository,
      MoneyAllocationRepository moneyAllocationRepository,
      SpendingItemRepository spendingItemRepository,
      SpendingMoneyAllocationRepository spendingMoneyAllocationRepository,
      SpendingEventRepository spendingEventRepository,
      ProcurementItemRepository procurementItemRepository,
      ProcurementQuoteRepository procurementQuoteRepository,
      ProcurementQuoteFileRepository procurementQuoteFileRepository,
      ProcurementEventRepository procurementEventRepository,
      ProcurementEventFileRepository procurementEventFileRepository,
      TrainingItemRepository trainingItemRepository,
      TrainingMoneyAllocationRepository trainingMoneyAllocationRepository,
      TravelItemRepository travelItemRepository,
      TravelMoneyAllocationRepository travelMoneyAllocationRepository,
      AuditService auditService) {
    this.fiscalYearRepository = fiscalYearRepository;
    this.moneyRepository = moneyRepository;
    this.categoryRepository = categoryRepository;
    this.spendingCategoryRepository = spendingCategoryRepository;
    this.fundingItemRepository = fundingItemRepository;
    this.moneyAllocationRepository = moneyAllocationRepository;
    this.spendingItemRepository = spendingItemRepository;
    this.spendingMoneyAllocationRepository = spendingMoneyAllocationRepository;
    this.spendingEventRepository = spendingEventRepository;
    this.procurementItemRepository = procurementItemRepository;
    this.procurementQuoteRepository = procurementQuoteRepository;
    this.procurementQuoteFileRepository = procurementQuoteFileRepository;
    this.procurementEventRepository = procurementEventRepository;
    this.procurementEventFileRepository = procurementEventFileRepository;
    this.trainingItemRepository = trainingItemRepository;
    this.trainingMoneyAllocationRepository = trainingMoneyAllocationRepository;
    this.travelItemRepository = travelItemRepository;
    this.travelMoneyAllocationRepository = travelMoneyAllocationRepository;
    this.auditService = auditService;
  }

  @Override
  public FiscalYear deepCloneFiscalYear(FiscalYear sourceFY, String targetFYName,
      ResponsibilityCentre targetRC) {

    logger.info("Deep cloning fiscal year '" + sourceFY.getName()
        + "' (ID: " + sourceFY.getId() + ") as '" + targetFYName + "'");

    // 1. Clone the FY shell
    FiscalYear clonedFY = new FiscalYear(targetFYName, sourceFY.getDescription(), targetRC);
    clonedFY.setShowSearchBox(sourceFY.getShowSearchBox());
    clonedFY.setShowCategoryFilter(sourceFY.getShowCategoryFilter());
    clonedFY.setGroupByCategory(sourceFY.getGroupByCategory());
    clonedFY.setOnTargetMin(sourceFY.getOnTargetMin());
    clonedFY.setOnTargetMax(sourceFY.getOnTargetMax());
    clonedFY.setActive(sourceFY.getActive());
    clonedFY = fiscalYearRepository.save(clonedFY);

    Long sourceFYId = sourceFY.getId();
    Long clonedFYId = clonedFY.getId();

    // 2. Clone Money types
    Map<Long, Money> moneyMap = cloneMoneyTypes(sourceFYId, clonedFY);
    logger.info("Cloned " + moneyMap.size() + " money types");

    // 3. Clone Categories
    Map<Long, Category> categoryMap = cloneCategories(sourceFYId, clonedFY);
    logger.info("Cloned " + categoryMap.size() + " categories");

    // 4. Clone Spending Categories
    int spendingCatCount = cloneSpendingCategories(sourceFYId, clonedFY);
    logger.info("Cloned " + spendingCatCount + " spending categories");

    // 5. Clone Procurement Items (with quotes, quote files, events, event files)
    Map<Long, ProcurementItem> procurementItemMap =
        cloneProcurementItems(sourceFYId, clonedFY, categoryMap);
    logger.info("Cloned " + procurementItemMap.size() + " procurement items");

    // 6. Clone Funding Items (with money allocations)
    int fundingItemCount = cloneFundingItems(sourceFYId, clonedFY, moneyMap, categoryMap);
    logger.info("Cloned " + fundingItemCount + " funding items");

    // 7. Clone Spending Items (with money allocations, events)
    int spendingItemCount = cloneSpendingItems(sourceFYId, clonedFY, moneyMap,
        categoryMap, procurementItemMap);
    logger.info("Cloned " + spendingItemCount + " spending items");

    // 8. Clone Training Items (with money allocations)
    int trainingItemCount = cloneTrainingItems(sourceFYId, clonedFY, moneyMap);
    logger.info("Cloned " + trainingItemCount + " training items");

    // 9. Clone Travel Items (with money allocations)
    int travelItemCount = cloneTravelItems(sourceFYId, clonedFY, moneyMap);
    logger.info("Cloned " + travelItemCount + " travel items");

    // 10. Clone Audit Events for this fiscal year
    auditService.cloneAuditEventsForFiscalYear(
        sourceFY.getResponsibilityCentre().getId(), sourceFYId,
        targetRC.getId(), targetRC.getName(),
        clonedFYId, targetFYName,
        "system-clone");
    logger.info("Cloned audit events for fiscal year");

    logger.info("Deep clone of fiscal year '" + sourceFY.getName()
        + "' completed as '" + targetFYName + "' (ID: " + clonedFYId + ")");

    return clonedFY;
  }

  /**
   * Clone all money types from a source FY to a cloned FY.
   *
   * @param sourceFYId the source fiscal year ID
   * @param clonedFY the cloned fiscal year entity
   * @return map of old Money ID → new Money entity
   */
  private Map<Long, Money> cloneMoneyTypes(Long sourceFYId, FiscalYear clonedFY) {
    Map<Long, Money> moneyMap = new HashMap<>();
    List<Money> sourceMonies = moneyRepository.findByFiscalYearId(sourceFYId);

    for (Money source : sourceMonies) {
      Money cloned = new Money();
      cloned.setCode(source.getCode());
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setIsDefault(source.getIsDefault());
      cloned.setDisplayOrder(source.getDisplayOrder());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());
      cloned = moneyRepository.save(cloned);
      moneyMap.put(source.getId(), cloned);
    }

    return moneyMap;
  }

  /**
   * Clone all categories from a source FY to a cloned FY.
   *
   * @param sourceFYId the source fiscal year ID
   * @param clonedFY the cloned fiscal year entity
   * @return map of old Category ID → new Category entity
   */
  private Map<Long, Category> cloneCategories(Long sourceFYId, FiscalYear clonedFY) {
    Map<Long, Category> categoryMap = new HashMap<>();
    List<Category> sourceCategories =
        categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId);

    for (Category source : sourceCategories) {
      Category cloned = new Category();
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setFundingType(source.getFundingType());
      cloned.setIsDefault(source.getIsDefault());
      cloned.setDisplayOrder(source.getDisplayOrder());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());
      cloned = categoryRepository.save(cloned);
      categoryMap.put(source.getId(), cloned);
    }

    return categoryMap;
  }

  /**
   * Clone all spending categories from a source FY to a cloned FY.
   *
   * @param sourceFYId the source fiscal year ID
   * @param clonedFY the cloned fiscal year entity
   * @return number of spending categories cloned
   */
  private int cloneSpendingCategories(Long sourceFYId, FiscalYear clonedFY) {
    List<SpendingCategory> sourceCategories =
        spendingCategoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(sourceFYId);
    int count = 0;

    for (SpendingCategory source : sourceCategories) {
      SpendingCategory cloned = new SpendingCategory(
          source.getName(), source.getDescription(), clonedFY,
          source.getIsDefault(), source.getDisplayOrder());
      cloned.setActive(source.getActive());
      spendingCategoryRepository.save(cloned);
      count++;
    }

    return count;
  }

  /**
   * Clone all procurement items (with quotes, quote files, events, event files)
   * from a source FY to a cloned FY.
   *
   * @param sourceFYId the source fiscal year ID
   * @param clonedFY the cloned fiscal year entity
   * @param categoryMap map of old Category ID → new Category entity
   * @return map of old ProcurementItem ID → new ProcurementItem entity
   */
  private Map<Long, ProcurementItem> cloneProcurementItems(Long sourceFYId,
      FiscalYear clonedFY, Map<Long, Category> categoryMap) {

    Map<Long, ProcurementItem> procurementItemMap = new HashMap<>();
    List<ProcurementItem> sourceItems =
        procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(
            sourceFYId);

    for (ProcurementItem source : sourceItems) {
      ProcurementItem cloned = new ProcurementItem();
      cloned.setPurchaseRequisition(source.getPurchaseRequisition());
      cloned.setPurchaseOrder(source.getPurchaseOrder());
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setVendor(source.getVendor());
      cloned.setContractNumber(source.getContractNumber());
      cloned.setContractStartDate(source.getContractStartDate());
      cloned.setContractEndDate(source.getContractEndDate());
      cloned.setFinalPrice(source.getFinalPrice());
      cloned.setFinalPriceCurrency(source.getFinalPriceCurrency());
      cloned.setFinalPriceExchangeRate(source.getFinalPriceExchangeRate());
      cloned.setFinalPriceCad(source.getFinalPriceCad());
      cloned.setQuotedPrice(source.getQuotedPrice());
      cloned.setQuotedPriceCurrency(source.getQuotedPriceCurrency());
      cloned.setQuotedPriceExchangeRate(source.getQuotedPriceExchangeRate());
      cloned.setQuotedPriceCad(source.getQuotedPriceCad());
      cloned.setProcurementCompleted(source.getProcurementCompleted());
      cloned.setProcurementCompletedDate(source.getProcurementCompletedDate());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());

      // Remap category
      if (source.getCategory() != null) {
        Category mappedCategory = categoryMap.get(source.getCategory().getId());
        cloned.setCategory(mappedCategory);
      }

      cloned = procurementItemRepository.save(cloned);
      procurementItemMap.put(source.getId(), cloned);

      // Clone quotes (with files)
      cloneProcurementQuotes(source, cloned);

      // Clone events (with files)
      cloneProcurementEvents(source, cloned);
    }

    return procurementItemMap;
  }

  /**
   * Clone all quotes (and their files) from a source procurement item to a cloned one.
   */
  private void cloneProcurementQuotes(ProcurementItem source, ProcurementItem cloned) {
    List<ProcurementQuote> sourceQuotes =
        procurementQuoteRepository.findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(
            source.getId());

    for (ProcurementQuote srcQuote : sourceQuotes) {
      ProcurementQuote clonedQuote = new ProcurementQuote();
      clonedQuote.setVendorName(srcQuote.getVendorName());
      clonedQuote.setVendorContact(srcQuote.getVendorContact());
      clonedQuote.setQuoteReference(srcQuote.getQuoteReference());
      clonedQuote.setAmount(srcQuote.getAmount());
      clonedQuote.setAmountCap(srcQuote.getAmountCap());
      clonedQuote.setAmountOm(srcQuote.getAmountOm());
      clonedQuote.setCurrency(srcQuote.getCurrency());
      clonedQuote.setExchangeRate(srcQuote.getExchangeRate());
      clonedQuote.setAmountCapCad(srcQuote.getAmountCapCad());
      clonedQuote.setAmountOmCad(srcQuote.getAmountOmCad());
      clonedQuote.setReceivedDate(srcQuote.getReceivedDate());
      clonedQuote.setExpiryDate(srcQuote.getExpiryDate());
      clonedQuote.setNotes(srcQuote.getNotes());
      clonedQuote.setStatus(srcQuote.getStatus());
      clonedQuote.setSelected(srcQuote.getSelected());
      clonedQuote.setProcurementItem(cloned);
      clonedQuote.setActive(srcQuote.getActive());
      clonedQuote.setCreatedBy(srcQuote.getCreatedBy());
      clonedQuote = procurementQuoteRepository.save(clonedQuote);

      // Clone quote files
      cloneProcurementQuoteFiles(srcQuote, clonedQuote);
    }
  }

  /**
   * Clone all files from a source procurement quote to a cloned one.
   */
  private void cloneProcurementQuoteFiles(ProcurementQuote srcQuote,
      ProcurementQuote clonedQuote) {
    List<ProcurementQuoteFile> sourceFiles =
        procurementQuoteFileRepository.findByQuoteIdAndActiveTrueOrderByFileNameAsc(
            srcQuote.getId());

    for (ProcurementQuoteFile srcFile : sourceFiles) {
      ProcurementQuoteFile clonedFile = new ProcurementQuoteFile();
      clonedFile.setFileName(srcFile.getFileName());
      clonedFile.setContentType(srcFile.getContentType());
      clonedFile.setFileSize(srcFile.getFileSize());
      clonedFile.setContent(srcFile.getContent());
      clonedFile.setDescription(srcFile.getDescription());
      clonedFile.setQuote(clonedQuote);
      clonedFile.setActive(srcFile.getActive());
      procurementQuoteFileRepository.save(clonedFile);
    }
  }

  /**
   * Clone all events (and their files) from a source procurement item to a cloned one.
   */
  private void cloneProcurementEvents(ProcurementItem source, ProcurementItem cloned) {
    List<ProcurementEvent> sourceEvents =
        procurementEventRepository.findByProcurementItemIdAndActiveTrue(source.getId());

    for (ProcurementEvent srcEvent : sourceEvents) {
      ProcurementEvent clonedEvent = new ProcurementEvent();
      clonedEvent.setProcurementItem(cloned);
      clonedEvent.setEventType(srcEvent.getEventType());
      clonedEvent.setEventDate(srcEvent.getEventDate());
      clonedEvent.setComment(srcEvent.getComment());
      clonedEvent.setNewStatus(srcEvent.getNewStatus());
      clonedEvent.setCreatedBy(srcEvent.getCreatedBy());
      clonedEvent.setActive(srcEvent.getActive());
      clonedEvent = procurementEventRepository.save(clonedEvent);

      // Clone event files
      cloneProcurementEventFiles(srcEvent, clonedEvent);
    }
  }

  /**
   * Clone all files from a source procurement event to a cloned one.
   */
  private void cloneProcurementEventFiles(ProcurementEvent srcEvent,
      ProcurementEvent clonedEvent) {
    List<ProcurementEventFile> sourceFiles =
        procurementEventFileRepository.findByEventIdAndActiveTrue(srcEvent.getId());

    for (ProcurementEventFile srcFile : sourceFiles) {
      ProcurementEventFile clonedFile = new ProcurementEventFile();
      clonedFile.setFileName(srcFile.getFileName());
      clonedFile.setContentType(srcFile.getContentType());
      clonedFile.setFileSize(srcFile.getFileSize());
      clonedFile.setContent(srcFile.getContent());
      clonedFile.setDescription(srcFile.getDescription());
      clonedFile.setEvent(clonedEvent);
      clonedFile.setActive(srcFile.getActive());
      procurementEventFileRepository.save(clonedFile);
    }
  }

  /**
   * Clone all funding items (with money allocations) from a source FY to a cloned FY.
   *
   * @param sourceFYId the source fiscal year ID
   * @param clonedFY the cloned fiscal year entity
   * @param moneyMap map of old Money ID → new Money entity
   * @param categoryMap map of old Category ID → new Category entity
   * @return number of funding items cloned
   */
  private int cloneFundingItems(Long sourceFYId, FiscalYear clonedFY,
      Map<Long, Money> moneyMap, Map<Long, Category> categoryMap) {

    List<FundingItem> sourceItems = fundingItemRepository.findByFiscalYearId(sourceFYId);
    int count = 0;

    for (FundingItem source : sourceItems) {
      FundingItem cloned = new FundingItem();
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setSource(source.getSource());
      cloned.setComments(source.getComments());
      cloned.setCurrency(source.getCurrency());
      cloned.setExchangeRate(source.getExchangeRate());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());

      // Remap category
      if (source.getCategory() != null) {
        Category mappedCategory = categoryMap.get(source.getCategory().getId());
        cloned.setCategory(mappedCategory);
      }

      cloned = fundingItemRepository.save(cloned);

      // Clone money allocations
      cloneMoneyAllocations(source, cloned, moneyMap);
      count++;
    }

    return count;
  }

  /**
   * Clone all money allocations from a source funding item to a cloned one.
   */
  private void cloneMoneyAllocations(FundingItem source, FundingItem cloned,
      Map<Long, Money> moneyMap) {
    List<MoneyAllocation> sourceAllocations =
        moneyAllocationRepository.findByFundingItemId(source.getId());

    for (MoneyAllocation srcAlloc : sourceAllocations) {
      Money mappedMoney = moneyMap.get(srcAlloc.getMoney().getId());
      if (mappedMoney == null) {
        logger.warning("Skipping money allocation — source Money ID "
            + srcAlloc.getMoney().getId() + " has no mapped clone");
        continue;
      }

      MoneyAllocation clonedAlloc = new MoneyAllocation();
      clonedAlloc.setFundingItem(cloned);
      clonedAlloc.setMoney(mappedMoney);
      clonedAlloc.setCapAmount(srcAlloc.getCapAmount());
      clonedAlloc.setOmAmount(srcAlloc.getOmAmount());
      moneyAllocationRepository.save(clonedAlloc);
    }
  }

  /**
   * Clone all spending items (with money allocations and events) from a source FY.
   *
   * @param sourceFYId the source fiscal year ID
   * @param clonedFY the cloned fiscal year entity
   * @param moneyMap map of old Money ID → new Money entity
   * @param categoryMap map of old Category ID → new Category entity
   * @param procurementItemMap map of old ProcurementItem ID → new ProcurementItem entity
   * @return number of spending items cloned
   */
  private int cloneSpendingItems(Long sourceFYId, FiscalYear clonedFY,
      Map<Long, Money> moneyMap, Map<Long, Category> categoryMap,
      Map<Long, ProcurementItem> procurementItemMap) {

    List<SpendingItem> sourceItems =
        spendingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId);
    int count = 0;

    for (SpendingItem source : sourceItems) {
      SpendingItem cloned = new SpendingItem();
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setVendor(source.getVendor());
      cloned.setReferenceNumber(source.getReferenceNumber());
      cloned.setAmount(source.getAmount());
      cloned.setEcoAmount(source.getEcoAmount());
      cloned.setStatus(source.getStatus());
      cloned.setCurrency(source.getCurrency());
      cloned.setExchangeRate(source.getExchangeRate());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());

      // Remap category (mandatory for spending items)
      Category mappedCategory = categoryMap.get(source.getCategory().getId());
      cloned.setCategory(mappedCategory);

      // Remap procurement item (optional)
      if (source.getProcurementItem() != null) {
        ProcurementItem mappedProcurement =
            procurementItemMap.get(source.getProcurementItem().getId());
        cloned.setProcurementItem(mappedProcurement);
      }

      cloned = spendingItemRepository.save(cloned);

      // Clone spending money allocations
      cloneSpendingMoneyAllocations(source, cloned, moneyMap);

      // Clone spending events
      cloneSpendingEvents(source, cloned);
      count++;
    }

    return count;
  }

  /**
   * Clone all spending money allocations from a source spending item to a cloned one.
   */
  private void cloneSpendingMoneyAllocations(SpendingItem source, SpendingItem cloned,
      Map<Long, Money> moneyMap) {
    List<SpendingMoneyAllocation> sourceAllocations =
        spendingMoneyAllocationRepository.findBySpendingItemId(source.getId());

    for (SpendingMoneyAllocation srcAlloc : sourceAllocations) {
      Money mappedMoney = moneyMap.get(srcAlloc.getMoney().getId());
      if (mappedMoney == null) {
        logger.warning("Skipping spending money allocation — source Money ID "
            + srcAlloc.getMoney().getId() + " has no mapped clone");
        continue;
      }

      SpendingMoneyAllocation clonedAlloc = new SpendingMoneyAllocation();
      clonedAlloc.setSpendingItem(cloned);
      clonedAlloc.setMoney(mappedMoney);
      clonedAlloc.setCapAmount(srcAlloc.getCapAmount());
      clonedAlloc.setOmAmount(srcAlloc.getOmAmount());
      spendingMoneyAllocationRepository.save(clonedAlloc);
    }
  }

  /**
   * Clone all spending events from a source spending item to a cloned one.
   */
  private void cloneSpendingEvents(SpendingItem source, SpendingItem cloned) {
    List<SpendingEvent> sourceEvents =
        spendingEventRepository.findBySpendingItemIdAndActiveTrue(source.getId());

    for (SpendingEvent srcEvent : sourceEvents) {
      SpendingEvent clonedEvent = new SpendingEvent(
          cloned, srcEvent.getEventType(), srcEvent.getEventDate(), srcEvent.getComment());
      clonedEvent.setCreatedBy(srcEvent.getCreatedBy());
      clonedEvent.setActive(srcEvent.getActive());
      spendingEventRepository.save(clonedEvent);
    }
  }

  /**
   * Clone all training items from a source FY to a cloned FY.
   */
  private int cloneTrainingItems(Long sourceFYId, FiscalYear clonedFY,
      Map<Long, Money> moneyMap) {
    List<TrainingItem> sourceItems =
        trainingItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId);
    int count = 0;

    for (TrainingItem source : sourceItems) {
      TrainingItem cloned = new TrainingItem();
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setProvider(source.getProvider());
      cloned.setReferenceNumber(source.getReferenceNumber());
      cloned.setEstimatedCost(source.getEstimatedCost());
      cloned.setActualCost(source.getActualCost());
      cloned.setStatus(source.getStatus());
      cloned.setTrainingType(source.getTrainingType());
      cloned.setCurrency(source.getCurrency());
      cloned.setExchangeRate(source.getExchangeRate());
      cloned.setStartDate(source.getStartDate());
      cloned.setEndDate(source.getEndDate());
      cloned.setLocation(source.getLocation());
      cloned.setEmployeeName(source.getEmployeeName());
      cloned.setNumberOfParticipants(source.getNumberOfParticipants());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());
      cloned = trainingItemRepository.save(cloned);

      // Clone training money allocations
      cloneTrainingMoneyAllocations(source, cloned, moneyMap);
      count++;
    }
    return count;
  }

  /**
   * Clone all training money allocations from a source training item to a cloned one.
   */
  private void cloneTrainingMoneyAllocations(TrainingItem source, TrainingItem cloned,
      Map<Long, Money> moneyMap) {
    List<TrainingMoneyAllocation> sourceAllocations =
        trainingMoneyAllocationRepository.findByTrainingItemId(source.getId());

    for (TrainingMoneyAllocation srcAlloc : sourceAllocations) {
      Money mappedMoney = moneyMap.get(srcAlloc.getMoney().getId());
      if (mappedMoney == null) {
        logger.warning("Skipping training money allocation — source Money ID "
            + srcAlloc.getMoney().getId() + " has no mapped clone");
        continue;
      }

      TrainingMoneyAllocation clonedAlloc = new TrainingMoneyAllocation();
      clonedAlloc.setTrainingItem(cloned);
      clonedAlloc.setMoney(mappedMoney);
      clonedAlloc.setOmAmount(srcAlloc.getOmAmount());
      trainingMoneyAllocationRepository.save(clonedAlloc);
    }
  }

  /**
   * Clone all travel items from a source FY to a cloned FY.
   */
  private int cloneTravelItems(Long sourceFYId, FiscalYear clonedFY,
      Map<Long, Money> moneyMap) {
    List<TravelItem> sourceItems =
        travelItemRepository.findByFiscalYearIdOrderByNameAsc(sourceFYId);
    int count = 0;

    for (TravelItem source : sourceItems) {
      TravelItem cloned = new TravelItem();
      cloned.setName(source.getName());
      cloned.setDescription(source.getDescription());
      cloned.setTravelAuthorizationNumber(source.getTravelAuthorizationNumber());
      cloned.setReferenceNumber(source.getReferenceNumber());
      cloned.setDestination(source.getDestination());
      cloned.setPurpose(source.getPurpose());
      cloned.setEstimatedCost(source.getEstimatedCost());
      cloned.setActualCost(source.getActualCost());
      cloned.setStatus(source.getStatus());
      cloned.setTravelType(source.getTravelType());
      cloned.setCurrency(source.getCurrency());
      cloned.setExchangeRate(source.getExchangeRate());
      cloned.setDepartureDate(source.getDepartureDate());
      cloned.setReturnDate(source.getReturnDate());
      cloned.setTravellerName(source.getTravellerName());
      cloned.setNumberOfTravellers(source.getNumberOfTravellers());
      cloned.setFiscalYear(clonedFY);
      cloned.setActive(source.getActive());
      cloned = travelItemRepository.save(cloned);

      // Clone travel money allocations
      cloneTravelMoneyAllocations(source, cloned, moneyMap);
      count++;
    }
    return count;
  }

  /**
   * Clone all travel money allocations from a source travel item to a cloned one.
   */
  private void cloneTravelMoneyAllocations(TravelItem source, TravelItem cloned,
      Map<Long, Money> moneyMap) {
    List<TravelMoneyAllocation> sourceAllocations =
        travelMoneyAllocationRepository.findByTravelItemId(source.getId());

    for (TravelMoneyAllocation srcAlloc : sourceAllocations) {
      Money mappedMoney = moneyMap.get(srcAlloc.getMoney().getId());
      if (mappedMoney == null) {
        logger.warning("Skipping travel money allocation — source Money ID "
            + srcAlloc.getMoney().getId() + " has no mapped clone");
        continue;
      }

      TravelMoneyAllocation clonedAlloc = new TravelMoneyAllocation();
      clonedAlloc.setTravelItem(cloned);
      clonedAlloc.setMoney(mappedMoney);
      clonedAlloc.setOmAmount(srcAlloc.getOmAmount());
      travelMoneyAllocationRepository.save(clonedAlloc);
    }
  }
}
