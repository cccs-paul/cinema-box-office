/*
 * myRC - Money Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Implementation of MoneyService for managing Money entities.
 */
package com.myrc.service;

import com.myrc.dto.MoneyDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.Money;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.MoneyAllocationRepository;
import com.myrc.repository.MoneyRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingMoneyAllocationRepository;
import com.myrc.repository.UserRepository;
import com.myrc.service.RCPermissionService;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of MoneyService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@Service
@Transactional
public class MoneyServiceImpl implements MoneyService {

  private static final Logger logger = Logger.getLogger(MoneyServiceImpl.class.getName());
  private static final String DEFAULT_MONEY_CODE = "AB";
  private static final String DEFAULT_MONEY_NAME = "A-Base";
  private static final String DEFAULT_MONEY_DESCRIPTION = "Default A-Base funding allocation";

  private final MoneyRepository moneyRepository;
  private final MoneyAllocationRepository moneyAllocationRepository;
  private final SpendingMoneyAllocationRepository spendingMoneyAllocationRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final RCPermissionService permissionService;

  public MoneyServiceImpl(MoneyRepository moneyRepository,
      MoneyAllocationRepository moneyAllocationRepository,
      SpendingMoneyAllocationRepository spendingMoneyAllocationRepository,
      FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository,
      RCPermissionService permissionService) {
    this.moneyRepository = moneyRepository;
    this.moneyAllocationRepository = moneyAllocationRepository;
    this.spendingMoneyAllocationRepository = spendingMoneyAllocationRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.permissionService = permissionService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MoneyDTO> getMoniesByFiscalYearId(Long fiscalYearId, String username) {
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

    List<Money> monies = moneyRepository.findByFiscalYearId(fiscalYearId);
    return monies.stream()
        .map(money -> {
          MoneyDTO dto = MoneyDTO.fromEntity(money);
          dto.setCanDelete(computeCanDelete(money));
          return dto;
        })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MoneyDTO> getMoneyById(Long moneyId, String username) {
    Optional<Money> moneyOpt = moneyRepository.findById(moneyId);
    if (moneyOpt.isEmpty()) {
      return Optional.empty();
    }

    Money money = moneyOpt.get();
    Long rcId = money.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      return Optional.empty();
    }

    MoneyDTO dto = MoneyDTO.fromEntity(money);
    dto.setCanDelete(computeCanDelete(money));
    return Optional.of(dto);
  }

  @Override
  public MoneyDTO createMoney(Long fiscalYearId, String username, String code, String name,
      String description) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has owner access to the RC (money types are configuration-level)
    if (!hasOwnerAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "Only owners can manage money types for this Responsibility Centre");
    }

    // Validate code
    if (code == null || code.trim().isEmpty()) {
      throw new IllegalArgumentException("Money code is required");
    }

    // Validate name
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Money name is required");
    }

    // Check if code already exists for this fiscal year
    if (moneyRepository.existsByCodeAndFiscalYear(code.toUpperCase(), fy)) {
      throw new IllegalArgumentException(
          "A Money with this code already exists for this Fiscal Year");
    }

    // Get next display order
    Integer maxOrder = moneyRepository.findMaxDisplayOrderByFiscalYearId(fiscalYearId);
    int nextOrder = (maxOrder != null ? maxOrder : 0) + 1;

    Money money = new Money(code.toUpperCase(), name, description, fy);
    money.setDisplayOrder(nextOrder);
    money.setIsDefault(false);

    Money saved = moneyRepository.save(money);
    logger.info("Created money " + code + " for fiscal year " + fy.getName() + " by user " + username);

    return MoneyDTO.fromEntity(saved);
  }

  @Override
  public MoneyDTO updateMoney(Long moneyId, String username, String code, String name,
      String description) {
    Optional<Money> moneyOpt = moneyRepository.findById(moneyId);
    if (moneyOpt.isEmpty()) {
      throw new IllegalArgumentException("Money not found");
    }

    Money money = moneyOpt.get();
    Long rcId = money.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has owner access to the RC (money types are configuration-level)
    if (!hasOwnerAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "Only owners can manage money types for this Responsibility Centre");
    }

    // Cannot modify default money code
    if (money.getIsDefault() && code != null && !money.getCode().equals(code.toUpperCase())) {
      throw new IllegalArgumentException("Cannot change the code of the default money (AB)");
    }

    // Check if new code conflicts with existing
    if (code != null && !money.getCode().equals(code.toUpperCase()) &&
        moneyRepository.existsByCodeAndFiscalYear(code.toUpperCase(), money.getFiscalYear())) {
      throw new IllegalArgumentException(
          "A Money with this code already exists for this Fiscal Year");
    }

    if (code != null && !money.getIsDefault()) {
      money.setCode(code.toUpperCase());
    }
    if (name != null) {
      money.setName(name);
    }
    if (description != null) {
      money.setDescription(description);
    }

    Money saved = moneyRepository.save(money);
    logger.info("Updated money " + money.getCode() + " by user " + username);

    return MoneyDTO.fromEntity(saved);
  }

  @Override
  public void deleteMoney(Long moneyId, String username) {
    Optional<Money> moneyOpt = moneyRepository.findById(moneyId);
    if (moneyOpt.isEmpty()) {
      throw new IllegalArgumentException("Money not found");
    }

    Money money = moneyOpt.get();
    Long rcId = money.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has owner access to the RC (money types are configuration-level)
    if (!hasOwnerAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "Only owners can manage money types for this Responsibility Centre");
    }

    // Cannot delete default money
    if (money.getIsDefault()) {
      throw new IllegalArgumentException("Cannot delete the default money (AB)");
    }

    // Check if money type is in use (has non-zero allocations)
    boolean fundingInUse = moneyAllocationRepository.hasNonZeroAllocationsByMoneyId(moneyId);
    boolean spendingInUse = spendingMoneyAllocationRepository.hasNonZeroAllocationsByMoneyId(moneyId);
    if (fundingInUse || spendingInUse) {
      throw new IllegalArgumentException(
          "Cannot delete money type \"" + money.getCode() +
          "\" because it is in use with non-zero funding or spending allocations");
    }

    // Delete any zero-amount allocations referencing this money to avoid FK constraint violations
    spendingMoneyAllocationRepository.deleteByMoneyId(moneyId);
    moneyAllocationRepository.deleteByMoney(money);

    moneyRepository.delete(money);
    logger.info("Deleted money " + money.getCode() + " from fiscal year " +
        money.getFiscalYear().getName() + " by user " + username);
  }

  @Override
  public void ensureDefaultMoneyExists(Long fiscalYearId) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      logger.warning("Cannot create default money: Fiscal Year " + fiscalYearId + " not found");
      return;
    }

    FiscalYear fy = fyOpt.get();

    // Check if default money already exists
    Optional<Money> existingDefault = moneyRepository.findByFiscalYearAndIsDefaultTrue(fy);
    if (existingDefault.isPresent()) {
      return;
    }

    // Check if AB already exists (might have been created without isDefault flag)
    Optional<Money> existingAB = moneyRepository.findByCodeAndFiscalYear(DEFAULT_MONEY_CODE, fy);
    if (existingAB.isPresent()) {
      Money ab = existingAB.get();
      ab.setIsDefault(true);
      moneyRepository.save(ab);
      return;
    }

    // Create default AB money
    Money defaultMoney = new Money(DEFAULT_MONEY_CODE, DEFAULT_MONEY_NAME, DEFAULT_MONEY_DESCRIPTION, fy, true);
    defaultMoney.setDisplayOrder(0);
    moneyRepository.save(defaultMoney);
    logger.info("Created default AB money for fiscal year " + fy.getName());
  }

  @Override
  public void reorderMonies(Long fiscalYearId, String username, List<Long> moneyIds) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has owner access to the RC (money types are configuration-level)
    if (!hasOwnerAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "Only owners can manage money types for this Responsibility Centre");
    }

    // Update display order for each money
    for (int i = 0; i < moneyIds.size(); i++) {
      Long moneyId = moneyIds.get(i);
      Optional<Money> moneyOpt = moneyRepository.findById(moneyId);
      if (moneyOpt.isPresent()) {
        Money money = moneyOpt.get();
        if (money.getFiscalYear().getId().equals(fiscalYearId)) {
          money.setDisplayOrder(i);
          moneyRepository.save(money);
        }
      }
    }

    logger.info("Reordered monies for fiscal year " + fy.getName() + " by user " + username);
  }

  /**
   * Compute whether a money type can be deleted.
   * Default money (AB) cannot be deleted. Non-default money can only be deleted
   * if all funding and spending allocations for it are zero.
   *
   * @param money the money entity
   * @return true if the money can be deleted
   */
  private boolean computeCanDelete(Money money) {
    if (Boolean.TRUE.equals(money.getIsDefault())) {
      return false;
    }
    boolean fundingInUse = moneyAllocationRepository.hasNonZeroAllocationsByMoneyId(money.getId());
    boolean spendingInUse = spendingMoneyAllocationRepository.hasNonZeroAllocationsByMoneyId(money.getId());
    return !fundingInUse && !spendingInUse;
  }

  /**
   * Check if user has any access to the RC.
   * Delegates to the centralized RCPermissionService which handles
   * local users, LDAP group-based access, and Demo RC visibility.
   */
  private boolean hasAccessToRC(Long rcId, String username) {
    return permissionService.hasAccess(rcId, username);
  }

  /**
   * Check if user has write access to the RC.
   * Delegates to the centralized RCPermissionService.
   */
  private boolean hasWriteAccessToRC(Long rcId, String username) {
    return permissionService.hasWriteAccess(rcId, username);
  }

  /**
   * Check if user has owner access to the RC.
   * Money type management is a configuration-level operation that requires OWNER access.
   * Delegates to the centralized RCPermissionService.
   */
  private boolean hasOwnerAccessToRC(Long rcId, String username) {
    return permissionService.isOwner(rcId, username);
  }
}
