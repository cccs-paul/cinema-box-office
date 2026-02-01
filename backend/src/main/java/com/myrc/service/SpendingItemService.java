/*
 * myRC - Spending Item Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Spending Item management operations.
 */
package com.myrc.service;

import com.myrc.dto.SpendingItemDTO;
import com.myrc.dto.SpendingMoneyAllocationDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Spending Item management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
public interface SpendingItemService {

  /**
   * Get all spending items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @return list of spending items
   * @throws IllegalArgumentException if user doesn't have access
   */
  List<SpendingItemDTO> getSpendingItemsByFiscalYearId(Long fiscalYearId, String username);

  /**
   * Get all spending items for a fiscal year filtered by category.
   *
   * @param fiscalYearId the fiscal year ID
   * @param categoryId the category ID to filter by
   * @param username the requesting user's username
   * @return list of spending items in the specified category
   * @throws IllegalArgumentException if user doesn't have access
   */
  List<SpendingItemDTO> getSpendingItemsByFiscalYearIdAndCategoryId(Long fiscalYearId, Long categoryId, String username);

  /**
   * Get a specific spending item by ID.
   *
   * @param spendingItemId the spending item ID
   * @param username the requesting user's username
   * @return optional spending item
   */
  Optional<SpendingItemDTO> getSpendingItemById(Long spendingItemId, String username);

  /**
   * Create a new spending item for a fiscal year.
   *
   * @param spendingItemDTO the spending item data
   * @param username the requesting user's username
   * @return the created spending item
   * @throws IllegalArgumentException if user doesn't have write access or validation fails
   */
  SpendingItemDTO createSpendingItem(SpendingItemDTO spendingItemDTO, String username);

  /**
   * Update an existing spending item.
   *
   * @param spendingItemId the spending item ID
   * @param spendingItemDTO the updated spending item data
   * @param username the requesting user's username
   * @return the updated spending item
   * @throws IllegalArgumentException if user doesn't have write access or validation fails
   */
  SpendingItemDTO updateSpendingItem(Long spendingItemId, SpendingItemDTO spendingItemDTO, String username);

  /**
   * Delete a spending item.
   *
   * @param spendingItemId the spending item ID
   * @param username the requesting user's username
   * @throws IllegalArgumentException if user doesn't have write access
   */
  void deleteSpendingItem(Long spendingItemId, String username);

  /**
   * Update the status of a spending item.
   *
   * @param spendingItemId the spending item ID
   * @param status the new status
   * @param username the requesting user's username
   * @return the updated spending item
   * @throws IllegalArgumentException if user doesn't have write access or status transition is invalid
   */
  SpendingItemDTO updateSpendingItemStatus(Long spendingItemId, String status, String username);

  /**
   * Get money allocations for a spending item.
   *
   * @param spendingItemId the spending item ID
   * @param username the requesting user's username
   * @return list of money allocations
   */
  List<SpendingMoneyAllocationDTO> getMoneyAllocations(Long spendingItemId, String username);

  /**
   * Update money allocations for a spending item.
   *
   * @param spendingItemId the spending item ID
   * @param allocations the new money allocations
   * @param username the requesting user's username
   * @return the updated spending item
   * @throws IllegalArgumentException if user doesn't have write access or validation fails
   */
  SpendingItemDTO updateMoneyAllocations(Long spendingItemId, List<SpendingMoneyAllocationDTO> allocations, String username);
}
