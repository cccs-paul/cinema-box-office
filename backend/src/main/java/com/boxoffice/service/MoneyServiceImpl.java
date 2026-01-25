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
package com.boxoffice.service;

import com.boxoffice.dto.MoneyDTO;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.Money;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.MoneyRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
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
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;

  public MoneyServiceImpl(MoneyRepository moneyRepository,
      FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository) {
    this.moneyRepository = moneyRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
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
        .map(MoneyDTO::fromEntity)
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

    return Optional.of(MoneyDTO.fromEntity(money));
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

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
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

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
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

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
    }

    // Cannot delete default money
    if (money.getIsDefault()) {
      throw new IllegalArgumentException("Cannot delete the default money (AB)");
    }

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

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
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
