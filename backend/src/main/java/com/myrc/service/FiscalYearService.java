/*
 * myRC - Fiscal Year Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import java.util.List;
import java.util.Optional;

import com.myrc.dto.FiscalYearDTO;

/**
 * Service interface for FiscalYear operations.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-22
 */
public interface FiscalYearService {

  /**
   * Get all fiscal years for a responsibility centre that the user has access to.
   *
   * @param rcId the responsibility centre ID
   * @param username the username
   * @return list of fiscal year DTOs
   */
  List<FiscalYearDTO> getFiscalYearsByRCId(Long rcId, String username);

  /**
   * Get a specific fiscal year by ID.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username
   * @return optional fiscal year DTO
   */
  Optional<FiscalYearDTO> getFiscalYearById(Long fiscalYearId, String username);

  /**
   * Create a new fiscal year for a responsibility centre.
   *
   * @param rcId the responsibility centre ID
   * @param username the username
   * @param name the fiscal year name
   * @param description the fiscal year description
   * @return the created fiscal year DTO
   */
  FiscalYearDTO createFiscalYear(Long rcId, String username, String name, String description);

  /**
   * Update an existing fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username
   * @param name the fiscal year name
   * @param description the fiscal year description
   * @return optional updated fiscal year DTO
   */
  Optional<FiscalYearDTO> updateFiscalYear(Long fiscalYearId, String username, String name,
                                            String description);

  /**
   * Delete a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username
   */
  void deleteFiscalYear(Long fiscalYearId, String username);

  /**
   * Update display settings for a fiscal year.
   * Only the RC owner can update display settings (including on-target thresholds).
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username (must be RC owner)
   * @param showSearchBox whether to show search box and filters
   * @param showCategoryFilter whether to show category filter
   * @param groupByCategory whether to group items by category
   * @param onTargetMin minimum percentage for "On Target" status (-100 to +100)
   * @param onTargetMax maximum percentage for "On Target" status (-100 to +100)
   * @return optional updated fiscal year DTO
   * @throws IllegalArgumentException if user is not the RC owner
   */
  Optional<FiscalYearDTO> updateDisplaySettings(Long fiscalYearId, String username,
                                                  Boolean showSearchBox, Boolean showCategoryFilter, Boolean groupByCategory,
                                                  Integer onTargetMin, Integer onTargetMax);

  /**
   * Toggle the active status of a fiscal year.
   * Only the RC owner can toggle the active status.
   * An inactive FY is accessible but read-only.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username (must be RC owner)
   * @return optional updated fiscal year DTO
   * @throws IllegalArgumentException if user is not the RC owner
   */
  Optional<FiscalYearDTO> toggleActiveStatus(Long fiscalYearId, String username);

  /**
   * Clone a fiscal year within the same responsibility centre.
   * Creates a deep copy of the fiscal year and all its child data
   * (monies, categories, funding items, spending items, procurement items, etc.).
   * The user must have write access to the RC.
   *
   * @param rcId the responsibility centre ID
   * @param fiscalYearId the fiscal year ID to clone
   * @param username the username
   * @param newName the name for the cloned fiscal year
   * @return the cloned fiscal year DTO
   * @throws IllegalArgumentException if user lacks write access or name already exists
   */
  FiscalYearDTO cloneFiscalYear(Long rcId, Long fiscalYearId, String username, String newName);

  /**
   * Clone a fiscal year to a different responsibility centre.
   * The user must have at least read access to the source RC and write access
   * to the target RC. Creates a deep copy of the fiscal year and all its child data.
   *
   * @param sourceRcId the source responsibility centre ID
   * @param fiscalYearId the fiscal year ID to clone
   * @param targetRcId the target responsibility centre ID
   * @param username the username
   * @param newName the name for the cloned fiscal year
   * @return the cloned fiscal year DTO
   * @throws IllegalArgumentException if user lacks required access or name already exists
   */
  FiscalYearDTO cloneFiscalYearToRC(Long sourceRcId, Long fiscalYearId, Long targetRcId,
                                     String username, String newName);
}
