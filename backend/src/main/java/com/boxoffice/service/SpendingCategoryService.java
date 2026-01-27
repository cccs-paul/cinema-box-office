/*
 * myRC - Spending Category Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Spending Category management operations.
 */
package com.boxoffice.service;

import com.boxoffice.dto.SpendingCategoryDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Spending Category management operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
public interface SpendingCategoryService {

  /**
   * Get all spending categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @return list of spending categories
   * @throws IllegalArgumentException if user doesn't have access
   */
  List<SpendingCategoryDTO> getCategoriesByFiscalYearId(Long fiscalYearId, String username);

  /**
   * Get a specific spending category by ID.
   *
   * @param categoryId the category ID
   * @param username the requesting user's username
   * @return optional spending category
   */
  Optional<SpendingCategoryDTO> getCategoryById(Long categoryId, String username);

  /**
   * Create a new spending category for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @param name the category name
   * @param description the category description
   * @return the created spending category
   * @throws IllegalArgumentException if user doesn't have write access or category name already exists
   */
  SpendingCategoryDTO createCategory(Long fiscalYearId, String username, String name, String description);

  /**
   * Update an existing spending category.
   *
   * @param categoryId the category ID
   * @param username the requesting user's username
   * @param name the new category name
   * @param description the new category description
   * @return the updated spending category
   * @throws IllegalArgumentException if user doesn't have write access or category is a default category
   */
  SpendingCategoryDTO updateCategory(Long categoryId, String username, String name, String description);

  /**
   * Delete a spending category.
   *
   * @param categoryId the category ID
   * @param username the requesting user's username
   * @throws IllegalArgumentException if user doesn't have write access or category is a default category
   */
  void deleteCategory(Long categoryId, String username);

  /**
   * Ensure default categories exist for a fiscal year.
   * Creates the default spending categories if they don't exist:
   * - Compute
   * - GPUs
   * - Storage
   * - Software Licenses
   * - Small Procurement
   * - Contractors
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @return list of all spending categories including defaults
   */
  List<SpendingCategoryDTO> ensureDefaultCategoriesExist(Long fiscalYearId, String username);

  /**
   * Reorder spending categories.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @param categoryIds the list of category IDs in the new order
   * @return the reordered spending categories
   * @throws IllegalArgumentException if user doesn't have write access
   */
  List<SpendingCategoryDTO> reorderCategories(Long fiscalYearId, String username, List<Long> categoryIds);

  /**
   * Initialize default spending categories for a fiscal year (internal use).
   * This method is called during FY creation and does not require user authentication.
   *
   * @param fiscalYearId the fiscal year ID
   */
  void initializeDefaultCategories(Long fiscalYearId);
}
