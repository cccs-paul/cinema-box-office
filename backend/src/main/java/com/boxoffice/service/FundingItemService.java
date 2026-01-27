/*
 * myRC - Funding Item Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Service interface for FundingItem operations.
 */
package com.boxoffice.service;

import com.boxoffice.dto.FundingItemDTO;
import com.boxoffice.dto.MoneyAllocationDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for FundingItem operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
public interface FundingItemService {

  /**
   * Get all funding items for a fiscal year that the user has access to.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username
   * @return list of funding item DTOs
   */
  List<FundingItemDTO> getFundingItemsByFiscalYearId(Long fiscalYearId, String username);

  /**
   * Get a specific funding item by ID.
   *
   * @param fundingItemId the funding item ID
   * @param username the username
   * @return optional funding item DTO
   */
  Optional<FundingItemDTO> getFundingItemById(Long fundingItemId, String username);

  /**
   * Create a new funding item for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the username
   * @param name the funding item name
   * @param description the funding item description
   * @param budgetAmount the budget amount
   * @param status the status
   * @param currency the currency code (defaults to CAD if null)
   * @param exchangeRate the exchange rate to CAD (required if currency is not CAD)
   * @param categoryId the category ID (optional)
   * @param moneyAllocations the money allocations for CAP/OM amounts (optional)
   * @return the created funding item DTO
   */
  FundingItemDTO createFundingItem(Long fiscalYearId, String username, String name,
                                    String description, BigDecimal budgetAmount, String status,
                                    String currency, BigDecimal exchangeRate, Long categoryId,
                                    List<MoneyAllocationDTO> moneyAllocations);

  /**
   * Update an existing funding item.
   *
   * @param fundingItemId the funding item ID
   * @param username the username
   * @param name the funding item name
   * @param description the funding item description
   * @param budgetAmount the budget amount
   * @param status the status
   * @param currency the currency code (defaults to CAD if null)
   * @param exchangeRate the exchange rate to CAD (required if currency is not CAD)
   * @param categoryId the category ID (optional, null to not change, -1 to clear)
   * @param moneyAllocations the money allocations for CAP/OM amounts (optional)
   * @return optional updated funding item DTO
   */
  Optional<FundingItemDTO> updateFundingItem(Long fundingItemId, String username, String name,
                                              String description, BigDecimal budgetAmount, String status,
                                              String currency, BigDecimal exchangeRate, Long categoryId,
                                              List<MoneyAllocationDTO> moneyAllocations);

  /**
   * Delete a funding item.
   *
   * @param fundingItemId the funding item ID
   * @param username the username
   */
  void deleteFundingItem(Long fundingItemId, String username);
}
