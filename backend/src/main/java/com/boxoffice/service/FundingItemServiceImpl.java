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
package com.boxoffice.service;

import com.boxoffice.dto.FundingItemDTO;
import com.boxoffice.model.Currency;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.FundingItem;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.FundingItemRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

  private final FundingItemRepository fundingItemRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;

  public FundingItemServiceImpl(FundingItemRepository fundingItemRepository,
      FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository) {
    this.fundingItemRepository = fundingItemRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
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
      String description, BigDecimal budgetAmount, String status,
      String currency, BigDecimal exchangeRate) {
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

    // Parse status
    FundingItem.Status itemStatus = FundingItem.Status.DRAFT;
    if (status != null && !status.trim().isEmpty()) {
      try {
        itemStatus = FundingItem.Status.valueOf(status.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + status);
      }
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

    FundingItem fi = new FundingItem(name, description, budgetAmount, itemStatus, fy);
    fi.setCurrency(itemCurrency);
    fi.setExchangeRate(itemCurrency == Currency.CAD ? null : exchangeRate);
    FundingItem saved = fundingItemRepository.save(fi);

    return FundingItemDTO.fromEntity(saved);
  }

  @Override
  public Optional<FundingItemDTO> updateFundingItem(Long fundingItemId, String username, String name,
      String description, BigDecimal budgetAmount, String status,
      String currency, BigDecimal exchangeRate) {
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
    if (budgetAmount != null) {
      fi.setBudgetAmount(budgetAmount);
    }
    if (status != null && !status.trim().isEmpty()) {
      try {
        fi.setStatus(FundingItem.Status.valueOf(status.toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid status: " + status);
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

    FundingItem saved = fundingItemRepository.save(fi);
    return Optional.of(FundingItemDTO.fromEntity(saved));
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
