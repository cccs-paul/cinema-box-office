/*
 * myRC - Category Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-27
 * Version: 1.0.0
 *
 * Description:
 * Implementation of CategoryService for managing Category entities.
 */
package com.myrc.service;

import com.myrc.dto.CategoryDTO;
import com.myrc.model.Category;
import com.myrc.model.FiscalYear;
import com.myrc.model.FundingType;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CategoryService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

  private static final Logger logger = Logger.getLogger(CategoryServiceImpl.class.getName());

  // Default categories for both funding and spending
  // Format: {name, description, fundingType}
  // Software Licenses, Small Procurement, and Contractors are OM_ONLY by default
  private static final String[][] DEFAULT_CATEGORIES = {
      {"Compute", "Computing infrastructure and services", "BOTH"},
      {"GPUs", "Graphics Processing Units for AI/ML and rendering", "BOTH"},
      {"Storage", "Data storage and backup services", "BOTH"},
      {"Software Licenses", "Software licensing and subscriptions", "OM_ONLY"},
      {"Small Procurement", "Miscellaneous small purchases and equipment", "OM_ONLY"},
      {"Contractors", "External contractors and consulting services", "OM_ONLY"}
  };

  // Map of category names to their default funding types for quick lookup
  private static final Map<String, FundingType> DEFAULT_FUNDING_TYPES = Map.of(
      "Software Licenses", FundingType.OM_ONLY,
      "Small Procurement", FundingType.OM_ONLY,
      "Contractors", FundingType.OM_ONLY
  );

  private final CategoryRepository categoryRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;

  public CategoryServiceImpl(CategoryRepository categoryRepository,
      FiscalYearRepository fiscalYearRepository,
      ResponsibilityCentreRepository rcRepository,
      RCAccessRepository accessRepository,
      UserRepository userRepository) {
    this.categoryRepository = categoryRepository;
    this.fiscalYearRepository = fiscalYearRepository;
    this.rcRepository = rcRepository;
    this.accessRepository = accessRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CategoryDTO> getCategoriesByFiscalYearId(Long fiscalYearId, String username) {
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

    List<Category> categories = categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(fiscalYearId);
    return categories.stream()
        .map(CategoryDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CategoryDTO> getCategoryById(Long categoryId, String username) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isEmpty()) {
      return Optional.empty();
    }

    Category category = categoryOpt.get();
    Long rcId = category.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      return Optional.empty();
    }

    return Optional.of(CategoryDTO.fromEntity(category));
  }

  @Override
  public CategoryDTO createCategory(Long fiscalYearId, String username, String name, String description) {
    return createCategory(fiscalYearId, username, name, description, FundingType.BOTH);
  }

  @Override
  public CategoryDTO createCategory(Long fiscalYearId, String username, String name, String description, FundingType fundingType) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Validate name
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Category name is required");
    }

    // Check if category name already exists for this fiscal year
    if (categoryRepository.existsByNameAndFiscalYear(name, fy)) {
      throw new IllegalArgumentException("A category with this name already exists for this Fiscal Year");
    }

    // Get next display order
    int maxOrder = categoryRepository.getMaxDisplayOrderByFiscalYearId(fiscalYearId);
    int nextOrder = maxOrder + 1;

    // New categories are always custom (not default)
    FundingType ft = fundingType != null ? fundingType : FundingType.BOTH;
    Category category = new Category(name, description, fy, false, nextOrder, ft);

    Category saved = categoryRepository.save(category);
    logger.info("Created category '" + name + "' with funding type " + ft + " for fiscal year " + fy.getName() + " by user " + username);

    return CategoryDTO.fromEntity(saved);
  }

  @Override
  public CategoryDTO updateCategory(Long categoryId, String username, String name, String description) {
    return updateCategory(categoryId, username, name, description, null);
  }

  @Override
  public CategoryDTO updateCategory(Long categoryId, String username, String name, String description, FundingType fundingType) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isEmpty()) {
      throw new IllegalArgumentException("Category not found");
    }

    Category category = categoryOpt.get();
    Long rcId = category.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Cannot modify default categories
    if (category.getIsDefault()) {
      throw new IllegalArgumentException("Cannot modify a default category. Default categories are read-only.");
    }

    // Check if new name conflicts with existing
    if (name != null && !category.getName().equals(name) &&
        categoryRepository.existsByNameAndFiscalYear(name, category.getFiscalYear())) {
      throw new IllegalArgumentException("A category with this name already exists for this Fiscal Year");
    }

    if (name != null) {
      category.setName(name);
    }
    if (description != null) {
      category.setDescription(description);
    }
    if (fundingType != null) {
      category.setFundingType(fundingType);
    }

    Category saved = categoryRepository.save(category);
    logger.info("Updated category '" + category.getName() + "' by user " + username);

    return CategoryDTO.fromEntity(saved);
  }

  @Override
  public void deleteCategory(Long categoryId, String username) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isEmpty()) {
      throw new IllegalArgumentException("Category not found");
    }

    Category category = categoryOpt.get();
    Long rcId = category.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Cannot delete default categories
    if (category.getIsDefault()) {
      throw new IllegalArgumentException("Cannot delete a default category. Default categories are read-only.");
    }

    categoryRepository.delete(category);
    logger.info("Deleted category '" + category.getName() + "' from fiscal year " +
        category.getFiscalYear().getName() + " by user " + username);
  }

  @Override
  public List<CategoryDTO> ensureDefaultCategoriesExist(Long fiscalYearId, String username) {
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

    // Check if default categories already exist
    List<Category> existingDefaults = categoryRepository.findDefaultCategoriesByFiscalYearId(fiscalYearId);
    if (!existingDefaults.isEmpty()) {
      // Return all categories
      return getCategoriesByFiscalYearId(fiscalYearId, username);
    }

    // Create default categories
    for (int i = 0; i < DEFAULT_CATEGORIES.length; i++) {
      String categoryName = DEFAULT_CATEGORIES[i][0];
      String categoryDescription = DEFAULT_CATEGORIES[i][1];
      String fundingTypeStr = DEFAULT_CATEGORIES[i][2];
      FundingType fundingType = FundingType.valueOf(fundingTypeStr);

      // Check if category with this name already exists
      if (!categoryRepository.existsByNameAndFiscalYear(categoryName, fy)) {
        Category category = new Category(categoryName, categoryDescription, fy, true, i, fundingType);
        categoryRepository.save(category);
        logger.info("Created default category '" + categoryName + "' with funding type " + fundingType + " for fiscal year " + fy.getName());
      }
    }

    return getCategoriesByFiscalYearId(fiscalYearId, username);
  }

  @Override
  public List<CategoryDTO> reorderCategories(Long fiscalYearId, String username, List<Long> categoryIds) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      throw new IllegalArgumentException("Fiscal Year not found");
    }

    FiscalYear fy = fyOpt.get();
    Long rcId = fy.getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Update display order for each category
    for (int i = 0; i < categoryIds.size(); i++) {
      Long categoryId = categoryIds.get(i);
      Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
      if (categoryOpt.isPresent()) {
        Category category = categoryOpt.get();
        if (category.getFiscalYear().getId().equals(fiscalYearId)) {
          category.setDisplayOrder(i);
          categoryRepository.save(category);
        }
      }
    }

    logger.info("Reordered categories for fiscal year " + fy.getName() + " by user " + username);
    return getCategoriesByFiscalYearId(fiscalYearId, username);
  }

  @Override
  public void initializeDefaultCategories(Long fiscalYearId) {
    Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
    if (fyOpt.isEmpty()) {
      logger.warning("Cannot initialize default categories: Fiscal Year " + fiscalYearId + " not found");
      return;
    }

    FiscalYear fy = fyOpt.get();

    // Check if default categories already exist
    List<Category> existingDefaults = categoryRepository.findDefaultCategoriesByFiscalYearId(fiscalYearId);
    if (!existingDefaults.isEmpty()) {
      return;
    }

    // Create default categories
    for (int i = 0; i < DEFAULT_CATEGORIES.length; i++) {
      String categoryName = DEFAULT_CATEGORIES[i][0];
      String categoryDescription = DEFAULT_CATEGORIES[i][1];
      String fundingTypeStr = DEFAULT_CATEGORIES[i][2];
      FundingType fundingType = FundingType.valueOf(fundingTypeStr);

      // Check if category with this name already exists
      if (!categoryRepository.existsByNameAndFiscalYear(categoryName, fy)) {
        Category category = new Category(categoryName, categoryDescription, fy, true, i, fundingType);
        categoryRepository.save(category);
        logger.info("Created default category '" + categoryName + "' with funding type " + fundingType + " for fiscal year " + fy.getName());
      }
    }
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

    // Demo RC is accessible to all users in read-only mode
    if ("Demo".equals(rc.getName())) {
      return true;
    }

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
