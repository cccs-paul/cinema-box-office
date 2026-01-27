/*
 * myRC - Category Service Interface
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-27
 * Version: 1.0.0
 *
 * Description:
 * Service interface for Category management operations.
 */
package com.boxoffice.service;

import com.boxoffice.dto.CategoryDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Category management operations.
 * Categories are used for grouping both funding and spending items.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 */
public interface CategoryService {

  /**
   * Get all categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @return list of categories
   * @throws IllegalArgumentException if user doesn't have access
   */
  List<CategoryDTO> getCategoriesByFiscalYearId(Long fiscalYearId, String username);

  /**
   * Get a specific category by ID.
   *
   * @param categoryId the category ID
   * @param username the requesting user's username
   * @return optional category
   */
  Optional<CategoryDTO> getCategoryById(Long categoryId, String username);

  /**
   * Create a new category for a fiscal year.
   * Only custom categories can be created - default categories are system-managed.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @param name the category name
   * @param description the category description
   * @return the created category
   * @throws IllegalArgumentException if user doesn't have write access or category name already exists
   */
  CategoryDTO createCategory(Long fiscalYearId, String username, String name, String description);

  /**
   * Update an existing category.
   * Only custom categories can be updated - default categories are read-only.
   *
   * @param categoryId the category ID
   * @param username the requesting user's username
   * @param name the new category name
   * @param description the new category description
   * @return the updated category
   * @throws IllegalArgumentException if user doesn't have write access or category is a default category
   */
  CategoryDTO updateCategory(Long categoryId, String username, String name, String description);

  /**
   * Delete a category.
   * Only custom categories can be deleted - default categories cannot be deleted.
   *
   * @param categoryId the category ID
   * @param username the requesting user's username
   * @throws IllegalArgumentException if user doesn't have write access or category is a default category
   */
  void deleteCategory(Long categoryId, String username);

  /**
   * Ensure default categories exist for a fiscal year.
   * Creates the default categories if they don't exist:
   * - Compute
   * - GPUs
   * - Storage
   * - Software Licenses
   * - Small Procurement
   * - Contractors
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @return list of all categories including defaults
   */
  List<CategoryDTO> ensureDefaultCategoriesExist(Long fiscalYearId, String username);

  /**
   * Reorder categories.
   *
   * @param fiscalYearId the fiscal year ID
   * @param username the requesting user's username
   * @param categoryIds the list of category IDs in the new order
   * @return the reordered categories
   * @throws IllegalArgumentException if user doesn't have write access
   */
  List<CategoryDTO> reorderCategories(Long fiscalYearId, String username, List<Long> categoryIds);

  /**
   * Initialize default categories for a fiscal year (internal use).
   * This method is called during FY creation and does not require user authentication.
   *
   * @param fiscalYearId the fiscal year ID
   */
  void initializeDefaultCategories(Long fiscalYearId);
}
