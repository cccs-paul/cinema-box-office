/*
 * myRC - Fiscal Year Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.boxoffice.service;

import com.boxoffice.dto.FiscalYearDTO;
import java.util.List;
import java.util.Optional;

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
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username
   * @param showCategoryFilter whether to show category filter
   * @param groupByCategory whether to group items by category
   * @param onTargetMin minimum percentage for "On Target" status (-100 to +100)
   * @param onTargetMax maximum percentage for "On Target" status (-100 to +100)
   * @return optional updated fiscal year DTO
   */
  Optional<FiscalYearDTO> updateDisplaySettings(Long fiscalYearId, String username,
                                                  Boolean showCategoryFilter, Boolean groupByCategory,
                                                  Integer onTargetMin, Integer onTargetMax);
}
