/*
 * myRC - Funding Item Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Implementation of FundingItemService.
 */
package com.myrc.service;

import com.myrc.dto.FundingItemDTO;
import com.myrc.dto.MoneyAllocationDTO;
import com.myrc.model.Category;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingItem;
import com.myrc.model.FundingSource;
import com.myrc.model.Money;
import com.myrc.model.MoneyAllocation;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of FundingItemService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@Service
@Transactional
public class FundingItemServiceImpl implements FundingItemService {

  private static final Logger logger = Logger.getLogger(FundingItemServiceImpl.class.getName());

  private final FundingItemRepository fundingItemRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final MoneyRepository moneyRepository;
  private final MoneyAllocationRepository moneyAllocationRepository;
  private final CategoryRepository categoryRepository;

  public FundingItemServiceImpl(FundingItemRepository fundingItemRepository,
      FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository,
      MoneyRepository moneyRepository,
      MoneyAllocationRepository moneyAllocationRepository,
      CategoryRepository categoryRepository) {
    this.fundingItemRepository = fundingItemRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.moneyRepository = moneyRepository;
    this.moneyAllocationRepository = moneyAllocationRepository;
    this.categoryRepository = categoryRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FundingItemDTO> getFundingItemsByFiscalYearId(Long fiscalYearId, String username) {
    // Get fiscal year and verify access to its RC
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

    List<FundingItem> fundingItems = fundingItemRepository.findByFiscalYearIdOrderByNameAsc(fiscalYearId);
    return fundingItems.stream()
        .map(FundingItemDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<FundingItemDTO> getFundingItemById(Long fundingItemId, String username) {
    Optional<FundingItem> fiOpt = fundingItemRepository.findById(fundingItemId);
    if (fiOpt.isEmpty()) {
      return Optional.empty();
    }

    FundingItem fi = fiOpt.get();
    Long rcId = fi.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      return Optional.empty();
    }

    return Optional.of(FundingItemDTO.fromEntity(fi));
  }

  @Override
  public FundingItemDTO createFundingItem(Long fiscalYearId, String username, String name,
      String description, String source,
      String comments, String currency, BigDecimal exchangeRate, Long categoryId,
      List<MoneyAllocationDTO> moneyAllocations) {
    // Get fiscal year and verify write access to its RC
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
    }

    // Check if name already exists for this fiscal year
    if (fundingItemRepository.existsByNameAndFiscalYear(name, fy)) {
      throw new IllegalArgumentException(
          "A Funding Item with this name already exists for this Fiscal Year");
    }

    // Validate name
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }

    // Parse funding source (mandatory, defaults to BUSINESS_PLAN)
    FundingSource itemSource = FundingSource.BUSINESS_PLAN;
    if (source != null && !source.trim().isEmpty()) {
      itemSource = FundingSource.fromString(source);
    }

    // Parse currency (default to CAD)
    Currency itemCurrency = Currency.CAD;
    if (currency != null && !currency.trim().isEmpty()) {
      itemCurrency = Currency.fromCode(currency);
      if (itemCurrency == null) {
        throw new IllegalArgumentException("Invalid currency: " + currency);
      }
    }

    // Validate exchange rate for non-CAD currencies
    if (itemCurrency != Currency.CAD && exchangeRate == null) {
      throw new IllegalArgumentException("Exchange rate is required for non-CAD currencies");
    }
    if (itemCurrency != Currency.CAD && exchangeRate != null && 
        exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Exchange rate must be greater than zero");
    }

    // Validate that at least one money allocation has a value > $0.00
    if (!hasValidMoneyAllocation(moneyAllocations)) {
      throw new IllegalArgumentException(
          "At least one money type must have a CAP or OM amount greater than $0.00");
    }

    // Get category if provided
    Category category = null;
    if (categoryId != null) {
      Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
      if (categoryOpt.isEmpty()) {
        throw new IllegalArgumentException("Category not found");
      }
      category = categoryOpt.get();
      // Verify category belongs to the same fiscal year
      if (!category.getFiscalYear().getId().equals(fy.getId())) {
        throw new IllegalArgumentException("Category does not belong to the specified Fiscal Year");
      }
    }

    FundingItem fi = new FundingItem(name, description, itemSource, fy);
    fi.setCurrency(itemCurrency);
    fi.setExchangeRate(itemCurrency == Currency.CAD ? null : exchangeRate);
    fi.setCategory(category);
    fi.setComments(comments);
    FundingItem saved = fundingItemRepository.save(fi);

    // Process money allocations - create default allocations for all FY monies
    createDefaultMoneyAllocations(saved, fy, moneyAllocations);

    // Reload to get allocations
    saved = fundingItemRepository.findById(saved.getId()).orElse(saved);
    logger.info("Created funding item '" + name + "' with money allocations for FY: " + fy.getName());

    return FundingItemDTO.fromEntity(saved);
  }

  /**
   * Create default money allocations for a funding item based on FY's configured monies.
   * Each allocation defaults to $0.00 CAD for both CAP and OM unless provided in the request.
   */
  private void createDefaultMoneyAllocations(FundingItem fundingItem, FiscalYear fy,
      List<MoneyAllocationDTO> requestedAllocations) {
    // Get all monies for this fiscal year
    List<Money> fyMonies = moneyRepository.findByFiscalYearId(fy.getId());

    for (Money money : fyMonies) {
      BigDecimal capAmount = BigDecimal.ZERO;
      BigDecimal omAmount = BigDecimal.ZERO;

      // Check if allocation was provided for this money
      if (requestedAllocations != null) {
        for (MoneyAllocationDTO reqAlloc : requestedAllocations) {
          if (reqAlloc.getMoneyId() != null && reqAlloc.getMoneyId().equals(money.getId())) {
            capAmount = reqAlloc.getCapAmount() != null ? reqAlloc.getCapAmount() : BigDecimal.ZERO;
            omAmount = reqAlloc.getOmAmount() != null ? reqAlloc.getOmAmount() : BigDecimal.ZERO;
            break;
          }
        }
      }

      MoneyAllocation allocation = new MoneyAllocation(fundingItem, money, capAmount, omAmount);
      fundingItem.addMoneyAllocation(allocation);
    }

    fundingItemRepository.save(fundingItem);
  }

  @Override
  public Optional<FundingItemDTO> updateFundingItem(Long fundingItemId, String username, String name,
      String description, String source,
      String comments, String currency, BigDecimal exchangeRate, Long categoryId,
      List<MoneyAllocationDTO> moneyAllocations) {
    Optional<FundingItem> fiOpt = fundingItemRepository.findById(fundingItemId);
    if (fiOpt.isEmpty()) {
      return Optional.empty();
    }

    FundingItem fi = fiOpt.get();
    Long rcId = fi.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
    }

    // Check if new name conflicts with existing
    if (name != null && !fi.getName().equals(name) &&
        fundingItemRepository.existsByNameAndFiscalYear(name, fi.getFiscalYear())) {
      throw new IllegalArgumentException(
          "A Funding Item with this name already exists for this Fiscal Year");
    }

    if (name != null) {
      fi.setName(name);
    }
    if (description != null) {
      fi.setDescription(description);
    }
    if (source != null && !source.trim().isEmpty()) {
      fi.setSource(FundingSource.fromString(source));
    }
    if (comments != null) {
      fi.setComments(comments);
    }

    // Handle category update
    if (categoryId != null) {
      if (categoryId == -1L) {
        // Clear category
        fi.setCategory(null);
      } else {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
          throw new IllegalArgumentException("Category not found");
        }
        Category category = categoryOpt.get();
        // Verify category belongs to the same fiscal year
        if (!category.getFiscalYear().getId().equals(fi.getFiscalYear().getId())) {
          throw new IllegalArgumentException("Category does not belong to the specified Fiscal Year");
        }
        fi.setCategory(category);
      }
    }

    // Handle currency update
    if (currency != null && !currency.trim().isEmpty()) {
      Currency itemCurrency = Currency.fromCode(currency);
      if (itemCurrency == null) {
        throw new IllegalArgumentException("Invalid currency: " + currency);
      }
      fi.setCurrency(itemCurrency);

      // Validate exchange rate for non-CAD currencies
      if (itemCurrency != Currency.CAD) {
        if (exchangeRate == null && fi.getExchangeRate() == null) {
          throw new IllegalArgumentException("Exchange rate is required for non-CAD currencies");
        }
        if (exchangeRate != null) {
          if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be greater than zero");
          }
          fi.setExchangeRate(exchangeRate);
        }
      } else {
        // Clear exchange rate for CAD
        fi.setExchangeRate(null);
      }
    } else if (exchangeRate != null) {
      // Updating exchange rate without changing currency
      Currency currentCurrency = fi.getCurrency() != null ? fi.getCurrency() : Currency.CAD;
      if (currentCurrency != Currency.CAD) {
        if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
          throw new IllegalArgumentException("Exchange rate must be greater than zero");
        }
        fi.setExchangeRate(exchangeRate);
      }
    }

    // Update money allocations if provided
    if (moneyAllocations != null && !moneyAllocations.isEmpty()) {
      updateMoneyAllocations(fi, moneyAllocations);
    }

    FundingItem saved = fundingItemRepository.save(fi);
    logger.info("Updated funding item '" + fi.getName() + "'");
    return Optional.of(FundingItemDTO.fromEntity(saved));
  }

  /**
   * Update money allocations for a funding item.
   */
  private void updateMoneyAllocations(FundingItem fundingItem, List<MoneyAllocationDTO> allocationDTOs) {
    for (MoneyAllocationDTO dto : allocationDTOs) {
      if (dto.getMoneyId() == null) {
        continue;
      }

      // Find existing allocation or create new one
      Optional<MoneyAllocation> existingAlloc = fundingItem.getMoneyAllocations().stream()
          .filter(a -> a.getMoney().getId().equals(dto.getMoneyId()))
          .findFirst();

      if (existingAlloc.isPresent()) {
        // Update existing allocation
        MoneyAllocation allocation = existingAlloc.get();
        allocation.setCapAmount(dto.getCapAmount() != null ? dto.getCapAmount() : BigDecimal.ZERO);
        allocation.setOmAmount(dto.getOmAmount() != null ? dto.getOmAmount() : BigDecimal.ZERO);
      } else {
        // Create new allocation
        Optional<Money> moneyOpt = moneyRepository.findById(dto.getMoneyId());
        if (moneyOpt.isPresent()) {
          MoneyAllocation newAlloc = new MoneyAllocation(
              fundingItem,
              moneyOpt.get(),
              dto.getCapAmount() != null ? dto.getCapAmount() : BigDecimal.ZERO,
              dto.getOmAmount() != null ? dto.getOmAmount() : BigDecimal.ZERO
          );
          fundingItem.addMoneyAllocation(newAlloc);
        }
      }
    }
  }

  @Override
  public void deleteFundingItem(Long fundingItemId, String username) {
    Optional<FundingItem> fiOpt = fundingItemRepository.findById(fundingItemId);
    if (fiOpt.isEmpty()) {
      throw new IllegalArgumentException("Funding Item not found");
    }

    FundingItem fi = fiOpt.get();
    Long rcId = fi.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
    }

    fundingItemRepository.delete(fi);
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

  /**
   * Check if at least one money allocation has a CAP or OM value greater than $0.00.
   *
   * @param moneyAllocations the list of money allocation DTOs
   * @return true if at least one allocation has a positive value, false otherwise
   */
  private boolean hasValidMoneyAllocation(List<MoneyAllocationDTO> moneyAllocations) {
    if (moneyAllocations == null || moneyAllocations.isEmpty()) {
      return false;
    }
    return moneyAllocations.stream().anyMatch(allocation ->
        (allocation.getCapAmount() != null && allocation.getCapAmount().compareTo(BigDecimal.ZERO) > 0) ||
        (allocation.getOmAmount() != null && allocation.getOmAmount().compareTo(BigDecimal.ZERO) > 0)
    );
  }
}
