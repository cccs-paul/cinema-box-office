/*
 * myRC - Fiscal Year Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import com.boxoffice.dto.FiscalYearDTO;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.RCAccess;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.model.User;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.RCAccessRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import com.boxoffice.repository.UserRepository;
import java.util.logging.Logger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of FiscalYearService.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-22
 */
@Service
@Transactional
public class FiscalYearServiceImpl implements FiscalYearService {

  private static final Logger logger = Logger.getLogger(FiscalYearServiceImpl.class.getName());

  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;
  private final MoneyService moneyService;

  public FiscalYearServiceImpl(FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository, RCAccessRepository accessRepository,
      UserRepository userRepository, MoneyService moneyService) {
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.moneyService = moneyService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FiscalYearDTO> getFiscalYearsByRCId(Long rcId, String username) {
    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
    }

    List<FiscalYear> fiscalYears = fiscalYearRepository.findByResponsibilityCentreId(rcId);
    return fiscalYears.stream()
        .map(FiscalYearDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<FiscalYearDTO> getFiscalYearById(Long fiscalYearId, String username) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      return Optional.empty();
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      return Optional.empty();
    }

    return Optional.of(FiscalYearDTO.fromEntity(fy));
  }

  @Override
  public FiscalYearDTO createFiscalYear(Long rcId, String username, String name, String description) {
    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
    }

    Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
    if (rcOpt.isEmpty()) {
      throw new IllegalArgumentException("Responsibility Centre not found");
    }

    ResponsibilityCentre rc = rcOpt.get();

    // Check if name already exists for this RC
    if (fiscalYearRepository.existsByNameAndResponsibilityCentre(name, rc)) {
      throw new IllegalArgumentException(
          "A Fiscal Year with this name already exists for this Responsibility Centre");
    }

    // Validate name
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }

    FiscalYear fy = new FiscalYear(name, description, rc);
    FiscalYear saved = fiscalYearRepository.save(fy);

    // Create default AB money for the new fiscal year
    moneyService.ensureDefaultMoneyExists(saved.getId());
    logger.info("Created fiscal year '" + name + "' with default AB money for RC: " + rc.getName());

    return FiscalYearDTO.fromEntity(saved);
  }

  @Override
  public Optional<FiscalYearDTO> updateFiscalYear(Long fiscalYearId, String username, String name,
      String description) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      return Optional.empty();
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to this Responsibility Centre");
    }

    // Check if new name conflicts with existing
    if (name != null && !fy.getName().equals(name) &&
        fiscalYearRepository.existsByNameAndResponsibilityCentre(name, fy.getResponsibilityCentre())) {
      throw new IllegalArgumentException(
          "A Fiscal Year with this name already exists for this Responsibility Centre");
    }

    if (name != null) {
      fy.setName(name);
    }
    if (description != null) {
      fy.setDescription(description);
    }

    FiscalYear saved = fiscalYearRepository.save(fy);
    return Optional.of(FiscalYearDTO.fromEntity(saved));
  }

  @Override
  public void deleteFiscalYear(Long fiscalYearId, String username) {
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

    fiscalYearRepository.delete(fy);
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
