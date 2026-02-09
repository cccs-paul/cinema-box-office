/*
 * myRC - Fiscal Year Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import com.myrc.dto.FiscalYearDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
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
  private final CategoryService categoryService;
  private final RCPermissionService permissionService;
  private final FiscalYearCloneService fiscalYearCloneService;

  public FiscalYearServiceImpl(FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository, RCAccessRepository accessRepository,
      UserRepository userRepository, MoneyService moneyService,
      CategoryService categoryService, RCPermissionService permissionService,
      FiscalYearCloneService fiscalYearCloneService) {
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
    this.moneyService = moneyService;
    this.categoryService = categoryService;
    this.permissionService = permissionService;
    this.fiscalYearCloneService = fiscalYearCloneService;
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
    
    // Create default categories for the new fiscal year
    categoryService.initializeDefaultCategories(saved.getId());
    
    logger.info("Created fiscal year '" + name + "' with default AB money and categories for RC: " + rc.getName());

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

  @Override
  public Optional<FiscalYearDTO> updateDisplaySettings(Long fiscalYearId, String username,
      Boolean showSearchBox, Boolean showCategoryFilter, Boolean groupByCategory,
      Integer onTargetMin, Integer onTargetMax) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      return Optional.empty();
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Only the RC owner can update display settings (including on-target thresholds)
    if (!isRCOwner(rcId, username)) {
      throw new IllegalArgumentException(
          "Only the RC owner can update display settings");
    }

    // Update settings if provided
    if (showSearchBox != null) {
      fy.setShowSearchBox(showSearchBox);
    }
    if (showCategoryFilter != null) {
      fy.setShowCategoryFilter(showCategoryFilter);
    }
    if (groupByCategory != null) {
      fy.setGroupByCategory(groupByCategory);
    }
    if (onTargetMin != null) {
      // Validate range (-100 to +100)
      int clampedMin = Math.max(-100, Math.min(100, onTargetMin));
      fy.setOnTargetMin(clampedMin);
    }
    if (onTargetMax != null) {
      // Validate range (-100 to +100)
      int clampedMax = Math.max(-100, Math.min(100, onTargetMax));
      fy.setOnTargetMax(clampedMax);
    }

    FiscalYear saved = fiscalYearRepository.save(fy);
    logger.info("Updated display settings for fiscal year '" + fy.getName() + 
        "' - showSearchBox: " + fy.getShowSearchBox() +
        ", showCategoryFilter: " + fy.getShowCategoryFilter() + 
        ", groupByCategory: " + fy.getGroupByCategory() +
        ", onTargetMin: " + fy.getOnTargetMin() +
        ", onTargetMax: " + fy.getOnTargetMax());
    return Optional.of(FiscalYearDTO.fromEntity(saved));
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
   * Check if user is the owner of the RC.
   * Delegates to the centralized RCPermissionService.
   */
  private boolean isRCOwner(Long rcId, String username) {
    return permissionService.isOwner(rcId, username);
  }

  @Override
  public Optional<FiscalYearDTO> toggleActiveStatus(Long fiscalYearId, String username) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      return Optional.empty();
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Only the RC owner can toggle active status
    if (!isRCOwner(rcId, username)) {
      throw new IllegalArgumentException(
          "Only the RC owner can toggle the active status of a fiscal year");
    }

    // Toggle the active status
    fy.setActive(!fy.getActive());
    FiscalYear saved = fiscalYearRepository.save(fy);
    logger.info("Toggled active status for fiscal year '" + fy.getName() + 
        "' to " + (fy.getActive() ? "active" : "inactive") + " by user " + username);
    return Optional.of(FiscalYearDTO.fromEntity(saved));
  }

  @Override
  public FiscalYearDTO cloneFiscalYear(Long rcId, Long fiscalYearId, String username,
      String newName) {
    // Verify user has at least read access to the RC (cloning only requires read access)
    if (!hasAccessToRC(rcId, username)) {
      throw new IllegalArgumentException(
          "User does not have access to this Responsibility Centre");
    }

    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear sourceFY = fyOpt.get();
    ResponsibilityCentre rc = sourceFY.getResponsibilityCentre();

    // Verify the FY belongs to the specified RC
    if (!rc.getId().equals(rcId)) {
      throw new IllegalArgumentException("Fiscal Year does not belong to this Responsibility Centre");
    }

    // Validate new name
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }

    // Check if name already exists for this RC
    if (fiscalYearRepository.existsByNameAndResponsibilityCentre(newName, rc)) {
      throw new IllegalArgumentException(
          "A Fiscal Year with this name already exists for this Responsibility Centre");
    }

    // Perform the deep clone
    FiscalYear clonedFY = fiscalYearCloneService.deepCloneFiscalYear(sourceFY, newName, rc);

    logger.info("Cloned fiscal year '" + sourceFY.getName() + "' as '" + newName
        + "' in RC '" + rc.getName() + "' by user " + username);

    return FiscalYearDTO.fromEntity(clonedFY);
  }

  @Override
  public FiscalYearDTO cloneFiscalYearToRC(Long sourceRcId, Long fiscalYearId, Long targetRcId,
      String username, String newName) {
    // Verify user has at least read access to the source RC
    if (!hasAccessToRC(sourceRcId, username)) {
      throw new IllegalArgumentException(
          "User does not have access to the source Responsibility Centre");
    }

    // Verify user has write access to the target RC
    if (!hasWriteAccessToRC(targetRcId, username)) {
      throw new IllegalArgumentException(
          "User does not have write access to the target Responsibility Centre");
    }

    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear sourceFY = fyOpt.get();

    // Verify the FY belongs to the specified source RC
    if (!sourceFY.getResponsibilityCentre().getId().equals(sourceRcId)) {
      throw new IllegalArgumentException(
          "Fiscal Year does not belong to the source Responsibility Centre");
    }

    Optional<ResponsibilityCentre> targetRcOpt = rcRepository.findById(targetRcId);
    if (targetRcOpt.isEmpty()) {
      throw new IllegalArgumentException("Target Responsibility Centre not found");
    }

    ResponsibilityCentre targetRC = targetRcOpt.get();

    // Validate new name
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }

    // Check if name already exists for the target RC
    if (fiscalYearRepository.existsByNameAndResponsibilityCentre(newName, targetRC)) {
      throw new IllegalArgumentException(
          "A Fiscal Year with this name already exists for the target Responsibility Centre");
    }

    // Perform the deep clone to the target RC
    FiscalYear clonedFY = fiscalYearCloneService.deepCloneFiscalYear(sourceFY, newName, targetRC);

    logger.info("Cloned fiscal year '" + sourceFY.getName() + "' as '" + newName
        + "' from RC '" + sourceFY.getResponsibilityCentre().getName()
        + "' to RC '" + targetRC.getName() + "' by user " + username);

    return FiscalYearDTO.fromEntity(clonedFY);
  }
}
