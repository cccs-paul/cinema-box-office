/*
 * myRC - Spending Item Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Implementation of SpendingItemService for managing SpendingItem entities.
 */
package com.myrc.service;

import com.myrc.dto.SpendingItemDTO;
import com.myrc.dto.SpendingMoneyAllocationDTO;
import com.myrc.model.Category;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.Money;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingItem;
import com.myrc.model.SpendingMoneyAllocation;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingItemRepository;
import com.myrc.repository.SpendingMoneyAllocationRepository;
import com.myrc.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SpendingItemService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Service
@Transactional
public class SpendingItemServiceImpl implements SpendingItemService {

  private static final Logger logger = Logger.getLogger(SpendingItemServiceImpl.class.getName());

  private final SpendingItemRepository spendingItemRepository;
  private final CategoryRepository categoryRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final MoneyRepository moneyRepository;
  private final SpendingMoneyAllocationRepository allocationRepository;

  public SpendingItemServiceImpl(SpendingItemRepository spendingItemRepository,
      CategoryRepository categoryRepository,
      FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository,
      MoneyRepository moneyRepository,
      SpendingMoneyAllocationRepository allocationRepository) {
    this.spendingItemRepository = spendingItemRepository;
    this.categoryRepository = categoryRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.moneyRepository = moneyRepository;
    this.allocationRepository = allocationRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SpendingItemDTO> getSpendingItemsByFiscalYearId(Long fiscalYearId, String username) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
    }

    List<SpendingItem> spendingItems = spendingItemRepository.findByFiscalYearIdOrderByNameAsc(fiscalYearId);
    return spendingItems.stream()
        .map(SpendingItemDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<SpendingItemDTO> getSpendingItemsByFiscalYearIdAndCategoryId(Long fiscalYearId, Long categoryId, String username) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
    }

    List<SpendingItem> spendingItems = spendingItemRepository.findByFiscalYearIdAndCategoryId(fiscalYearId, categoryId);
    return spendingItems.stream()
        .map(SpendingItemDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SpendingItemDTO> getSpendingItemById(Long spendingItemId, String username) {
    Optional<SpendingItem> siOpt = spendingItemRepository.findById(spendingItemId);
    if (siOpt.isEmpty()) {
      return Optional.empty();
    }

    SpendingItem si = siOpt.get();
    Long rcId = si.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      return Optional.empty();
    }

    return Optional.of(SpendingItemDTO.fromEntity(si));
  }

  @Override
  public SpendingItemDTO createSpendingItem(SpendingItemDTO dto, String username) {
    // Validate required fields
    if (dto.getFiscalYearId() == null) {
      throw new IllegalArgumentException("Fiscal Year ID is required");
    }
    if (dto.getName() == null || dto.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }
    if (dto.getCategoryId() == null) {
      throw new IllegalArgumentException("Category ID is required");
    }

    // Get fiscal year and verify write access
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(dto.getFiscalYearId());
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Get category
    Optional<Category> categoryOpt = categoryRepository.findById(dto.getCategoryId());
    if (categoryOpt.isEmpty()) {
      throw new IllegalArgumentException("Category not found");
    }

    Category category = categoryOpt.get();

    // Verify category belongs to the same fiscal year
    if (!category.getFiscalYear().getId().equals(fy.getId())) {
      throw new IllegalArgumentException("Category does not belong to the specified Fiscal Year");
    }

    // Parse status
    SpendingItem.Status itemStatus = SpendingItem.Status.DRAFT;
    if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
      try {
        itemStatus = SpendingItem.Status.valueOf(dto.getStatus().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
      }
    }

    // Parse currency
    Currency itemCurrency = Currency.CAD;
    if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
      itemCurrency = Currency.fromCode(dto.getCurrency());
      if (itemCurrency == null) {
        throw new IllegalArgumentException("Invalid currency: " + dto.getCurrency());
      }
    }

    // Validate exchange rate for non-CAD currencies
    if (itemCurrency != Currency.CAD && dto.getExchangeRate() == null) {
      throw new IllegalArgumentException("Exchange rate is required for non-CAD currencies");
    }
    if (itemCurrency != Currency.CAD && dto.getExchangeRate() != null && 
        dto.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Exchange rate must be greater than zero");
    }

    // Validate that at least one money allocation has a value > $0.00
    if (!hasValidMoneyAllocation(dto.getMoneyAllocations())) {
      throw new IllegalArgumentException(
          "At least one money type must have a CAP or OM amount greater than $0.00");
    }

    // Create spending item
    SpendingItem spendingItem = new SpendingItem();
    spendingItem.setName(dto.getName());
    spendingItem.setDescription(dto.getDescription());
    spendingItem.setVendor(dto.getVendor());
    spendingItem.setReferenceNumber(dto.getReferenceNumber());
    spendingItem.setAmount(dto.getAmount());
    spendingItem.setStatus(itemStatus);
    spendingItem.setCurrency(itemCurrency);
    spendingItem.setExchangeRate(itemCurrency == Currency.CAD ? null : dto.getExchangeRate());
    spendingItem.setCategory(category);
    spendingItem.setFiscalYear(fy);
    spendingItem.setActive(true);

    SpendingItem saved = spendingItemRepository.save(spendingItem);

    // Process money allocations
    createDefaultMoneyAllocations(saved, fy, dto.getMoneyAllocations());

    // Reload to get allocations
    saved = spendingItemRepository.findById(saved.getId()).orElse(saved);
    logger.info("Created spending item '" + dto.getName() + "' for FY: " + fy.getName() + " by user " + username);

    return SpendingItemDTO.fromEntity(saved);
  }

  /**
   * Create default money allocations for a spending item based on FY's configured monies.
   */
  private void createDefaultMoneyAllocations(SpendingItem spendingItem, FiscalYear fy,
      List<SpendingMoneyAllocationDTO> requestedAllocations) {
    // Get all monies for this fiscal year
    List<Money> fyMonies = moneyRepository.findByFiscalYearId(fy.getId());

    for (Money money : fyMonies) {
      BigDecimal capAmount = BigDecimal.ZERO;
      BigDecimal omAmount = BigDecimal.ZERO;

      // Check if allocation was provided for this money
      if (requestedAllocations != null) {
        for (SpendingMoneyAllocationDTO reqAlloc : requestedAllocations) {
          if (reqAlloc.getMoneyId() != null && reqAlloc.getMoneyId().equals(money.getId())) {
            capAmount = reqAlloc.getCapAmount() != null ? reqAlloc.getCapAmount() : BigDecimal.ZERO;
            omAmount = reqAlloc.getOmAmount() != null ? reqAlloc.getOmAmount() : BigDecimal.ZERO;
            break;
          }
        }
      }

      SpendingMoneyAllocation allocation = new SpendingMoneyAllocation(spendingItem, money, capAmount, omAmount);
      spendingItem.addMoneyAllocation(allocation);
    }

    spendingItemRepository.save(spendingItem);
  }

  @Override
  public SpendingItemDTO updateSpendingItem(Long spendingItemId, SpendingItemDTO dto, String username) {
    Optional<SpendingItem> siOpt = spendingItemRepository.findById(spendingItemId);
    if (siOpt.isEmpty()) {
      throw new IllegalArgumentException("Spending Item not found");
    }

    SpendingItem si = siOpt.get();
    Long rcId = si.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Update fields
    if (dto.getName() != null) {
      si.setName(dto.getName());
    }
    if (dto.getDescription() != null) {
      si.setDescription(dto.getDescription());
    }
    if (dto.getVendor() != null) {
      si.setVendor(dto.getVendor());
    }
    if (dto.getReferenceNumber() != null) {
      si.setReferenceNumber(dto.getReferenceNumber());
    }
    if (dto.getEcoAmount() != null) {
      si.setEcoAmount(dto.getEcoAmount());
    }
    if (dto.getAmount() != null) {
      si.setAmount(dto.getAmount());
    }
    if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
      try {
        si.setStatus(SpendingItem.Status.valueOf(dto.getStatus().toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
      }
    }

    // Handle currency update
    if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
      Currency itemCurrency = Currency.fromCode(dto.getCurrency());
      if (itemCurrency == null) {
        throw new IllegalArgumentException("Invalid currency: " + dto.getCurrency());
      }
      si.setCurrency(itemCurrency);

      if (itemCurrency != Currency.CAD) {
        if (dto.getExchangeRate() == null && si.getExchangeRate() == null) {
          throw new IllegalArgumentException("Exchange rate is required for non-CAD currencies");
        }
        if (dto.getExchangeRate() != null) {
          if (dto.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be greater than zero");
          }
          si.setExchangeRate(dto.getExchangeRate());
        }
      } else {
        si.setExchangeRate(null);
      }
    } else if (dto.getExchangeRate() != null) {
      Currency currentCurrency = si.getCurrency() != null ? si.getCurrency() : Currency.CAD;
      if (currentCurrency != Currency.CAD) {
        if (dto.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
          throw new IllegalArgumentException("Exchange rate must be greater than zero");
        }
        si.setExchangeRate(dto.getExchangeRate());
      }
    }

    // Handle category update
    if (dto.getCategoryId() != null) {
      Optional<Category> categoryOpt = categoryRepository.findById(dto.getCategoryId());
      if (categoryOpt.isEmpty()) {
        throw new IllegalArgumentException("Category not found");
      }
      Category category = categoryOpt.get();
      if (!category.getFiscalYear().getId().equals(si.getFiscalYear().getId())) {
        throw new IllegalArgumentException("Category does not belong to the specified Fiscal Year");
      }
      si.setCategory(category);
    }

    // Update money allocations if provided
    if (dto.getMoneyAllocations() != null && !dto.getMoneyAllocations().isEmpty()) {
      updateMoneyAllocations(si, dto.getMoneyAllocations());
    }

    SpendingItem saved = spendingItemRepository.save(si);
    logger.info("Updated spending item '" + si.getName() + "' by user " + username);

    return SpendingItemDTO.fromEntity(saved);
  }

  /**
   * Update money allocations for a spending item.
   */
  private void updateMoneyAllocations(SpendingItem spendingItem, List<SpendingMoneyAllocationDTO> allocationDTOs) {
    for (SpendingMoneyAllocationDTO dto : allocationDTOs) {
      if (dto.getMoneyId() == null) {
        continue;
      }

      // Find existing allocation or create new one
      Optional<SpendingMoneyAllocation> existingAlloc = spendingItem.getMoneyAllocations().stream()
          .filter(a -> a.getMoney().getId().equals(dto.getMoneyId()))
          .findFirst();

      if (existingAlloc.isPresent()) {
        // Update existing allocation
        SpendingMoneyAllocation allocation = existingAlloc.get();
        allocation.setCapAmount(dto.getCapAmount() != null ? dto.getCapAmount() : BigDecimal.ZERO);
        allocation.setOmAmount(dto.getOmAmount() != null ? dto.getOmAmount() : BigDecimal.ZERO);
      } else {
        // Create new allocation
        Optional<Money> moneyOpt = moneyRepository.findById(dto.getMoneyId());
        if (moneyOpt.isPresent()) {
          SpendingMoneyAllocation newAlloc = new SpendingMoneyAllocation(
              spendingItem,
              moneyOpt.get(),
              dto.getCapAmount() != null ? dto.getCapAmount() : BigDecimal.ZERO,
              dto.getOmAmount() != null ? dto.getOmAmount() : BigDecimal.ZERO
          );
          spendingItem.addMoneyAllocation(newAlloc);
        }
      }
    }
  }

  @Override
  public void deleteSpendingItem(Long spendingItemId, String username) {
    Optional<SpendingItem> siOpt = spendingItemRepository.findById(spendingItemId);
    if (siOpt.isEmpty()) {
      throw new IllegalArgumentException("Spending Item not found");
    }

    SpendingItem si = siOpt.get();
    Long rcId = si.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    spendingItemRepository.delete(si);
    logger.info("Deleted spending item '" + si.getName() + "' by user " + username);
  }

  @Override
  public SpendingItemDTO updateSpendingItemStatus(Long spendingItemId, String status, String username) {
    Optional<SpendingItem> siOpt = spendingItemRepository.findById(spendingItemId);
    if (siOpt.isEmpty()) {
      throw new IllegalArgumentException("Spending Item not found");
    }

    SpendingItem si = siOpt.get();
    Long rcId = si.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    try {
      SpendingItem.Status newStatus = SpendingItem.Status.valueOf(status.toUpperCase());
      si.setStatus(newStatus);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid status: " + status);
    }

    SpendingItem saved = spendingItemRepository.save(si);
    logger.info("Updated spending item '" + si.getName() + "' status to " + status + " by user " + username);

    return SpendingItemDTO.fromEntity(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SpendingMoneyAllocationDTO> getMoneyAllocations(Long spendingItemId, String username) {
    Optional<SpendingItem> siOpt = spendingItemRepository.findById(spendingItemId);
    if (siOpt.isEmpty()) {
      throw new IllegalArgumentException("Spending Item not found");
    }

    SpendingItem si = siOpt.get();
    Long rcId = si.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
    }

    return si.getMoneyAllocations().stream()
        .map(SpendingMoneyAllocationDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public SpendingItemDTO updateMoneyAllocations(Long spendingItemId, List<SpendingMoneyAllocationDTO> allocations, String username) {
    Optional<SpendingItem> siOpt = spendingItemRepository.findById(spendingItemId);
    if (siOpt.isEmpty()) {
      throw new IllegalArgumentException("Spending Item not found");
    }

    SpendingItem si = siOpt.get();
    Long rcId = si.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Validate that at least one allocation has a value > $0.00
    if (!hasValidMoneyAllocation(allocations)) {
      throw new IllegalArgumentException(
          "At least one money type must have a CAP or OM amount greater than $0.00");
    }

    updateMoneyAllocations(si, allocations);
    SpendingItem saved = spendingItemRepository.save(si);
    logger.info("Updated money allocations for spending item '" + si.getName() + "' by user " + username);

    return SpendingItemDTO.fromEntity(saved);
  }

  /**
   * Check if at least one money allocation has a CAP or OM value greater than $0.00.
   */
  private boolean hasValidMoneyAllocation(List<SpendingMoneyAllocationDTO> moneyAllocations) {
    if (moneyAllocations == null || moneyAllocations.isEmpty()) {
      return false;
    }
    return moneyAllocations.stream().anyMatch(allocation ->
        (allocation.getCapAmount() != null && allocation.getCapAmount().compareTo(BigDecimal.ZERO) > 0) ||
        (allocation.getOmAmount() != null && allocation.getOmAmount().compareTo(BigDecimal.ZERO) > 0)
    );
  }

  /**
   * Check if user has any access to the RC.
   */
  private boolean hasAccessToRC(Long rcId, String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return false;
    }

    ResponsibilityCentre rc = rcOpt.get();

    // Demo RC is accessible to all users in read-only mode
    if ("Demo".equals(rc.getName())) {
      return true;
    }

    // Check if owner
    if (rc.getOwner().getId().equals(user.getId())) {
      return true;
    }

    // Check if has access record
    Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(rc, user);
    return accessOpt.isPresent();
  }

  /**
   * Check if user has write access to the RC.
   */
  private boolean hasWriteAccessToRC(Long rcId, String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      return false;
    }

    ResponsibilityCentre rc = rcOpt.get();

    // Owner always has write access
    if (rc.getOwner().getId().equals(user.getId())) {
      return true;
    }

    // Check if has READ_WRITE access record
    Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(rc, user);
    return accessOpt.isPresent() && RCAccess.AccessLevel.READ_WRITE.equals(accessOpt.get().getAccessLevel());
  }
}
