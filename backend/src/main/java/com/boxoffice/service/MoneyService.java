/*
 * myRC - Money Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Money management operations.
 */
package com.boxoffice.service;

import com.boxoffice.dto.MoneyDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Money management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
public interface MoneyService {

  /**
   * Get all monies for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @return list of monies
   * @throws IllegalArgumentException if user doesn't have access
   */
  List<MoneyDTO> getMoniesByFiscalYearId(Long fiscalYearId, String username);

  /**
   * Get a specific money by ID.
   *
   * @param moneyId the money ID
   * @param username the requesting user's username
   * @return optional money
   */
  Optional<MoneyDTO> getMoneyById(Long moneyId, String username);

  /**
   * Create a new money for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @param code the money code
   * @param name the money name
   * @param description the money description
   * @return the created money
   * @throws IllegalArgumentException if user doesn't have write access or money code already exists
   */
  MoneyDTO createMoney(Long fiscalYearId, String username, String code, String name, String description);

  /**
   * Update an existing money.
   *
   * @param moneyId the money ID
   * @param username the requesting user's username
   * @param code the updated money code
   * @param name the updated money name
   * @param description the updated money description
   * @return the updated money
   * @throws IllegalArgumentException if user doesn't have write access or money is a system default
   */
  MoneyDTO updateMoney(Long moneyId, String username, String code, String name, String description);

  /**
   * Delete a money.
   *
   * @param moneyId the money ID
   * @param username the requesting user's username
   * @throws IllegalArgumentException if user doesn't have write access or money is a system default
   */
  void deleteMoney(Long moneyId, String username);

  /**
   * Create default money (AB) for a fiscal year if it doesn't exist.
   * This is called automatically when a new fiscal year is created.
   *
   * @param fiscalYearId the fiscal year ID
   */
  void ensureDefaultMoneyExists(Long fiscalYearId);

  /**
   * Reorder monies within a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @param moneyIds the ordered list of money IDs
   * @throws IllegalArgumentException if user doesn't have write access
   */
  void reorderMonies(Long fiscalYearId, String username, List<Long> moneyIds);
}
