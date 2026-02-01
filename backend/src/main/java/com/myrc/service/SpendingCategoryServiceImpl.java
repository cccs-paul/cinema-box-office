/*
 * myRC - Spending Category Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * Implementation of SpendingCategoryService for managing SpendingCategory entities.
 */
package com.myrc.service;

import com.myrc.dto.SpendingCategoryDTO;
import com.myrc.model.FiscalYear;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingCategory;
import com.myrc.model.User;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingCategoryRepository;
import com.myrc.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SpendingCategoryService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Service
@Transactional
public class SpendingCategoryServiceImpl implements SpendingCategoryService {

  private static final Logger logger = Logger.getLogger(SpendingCategoryServiceImpl.class.getName());

  // Default spending categories
  private static final String[][] DEFAULT_CATEGORIES = {
      {"Compute", "Computing infrastructure and services"},
      {"GPUs", "Graphics Processing Units for AI/ML and rendering"},
      {"Storage", "Data storage and backup services"},
      {"Software Licenses", "Software licensing and subscriptions"},
      {"Small Procurement", "Miscellaneous small purchases and equipment"},
      {"Contractors", "External contractors and consulting services"}
  };

  private final SpendingCategoryRepository categoryRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final ResponsibilityCentreRepository rcRepository;
  private final RCAccessRepository accessRepository;
  private final UserRepository userRepository;

  public SpendingCategoryServiceImpl(SpendingCategoryRepository categoryRepository,
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
  public List<SpendingCategoryDTO> getCategoriesByFiscalYearId(Long fiscalYearId, String username) {
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

    List<SpendingCategory> categories = categoryRepository.findByFiscalYearIdOrderByDisplayOrderAscNameAsc(fiscalYearId);
    return categories.stream()
        .map(SpendingCategoryDTO::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SpendingCategoryDTO> getCategoryById(Long categoryId, String username) {
    Optional<SpendingCategory> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isEmpty()) {
      return Optional.empty();
    }

    SpendingCategory category = categoryOpt.get();
    Long rcId = category.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has access to the RC
    if (!hasAccessToRC(rcId, username)) {
      return Optional.empty();
    }

    return Optional.of(SpendingCategoryDTO.fromEntity(category));
  }

  @Override
  public SpendingCategoryDTO createCategory(Long fiscalYearId, String username, String name, String description) {
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

    SpendingCategory category = new SpendingCategory(name, description, fy, false);
    category.setDisplayOrder(nextOrder);

    SpendingCategory saved = categoryRepository.save(category);
    logger.info("Created spending category '" + name + "' for fiscal year " + fy.getName() + " by user " + username);

    return SpendingCategoryDTO.fromEntity(saved);
  }

  @Override
  public SpendingCategoryDTO updateCategory(Long categoryId, String username, String name, String description) {
    Optional<SpendingCategory> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isEmpty()) {
      throw new IllegalArgumentException("Category not found");
    }

    SpendingCategory category = categoryOpt.get();
    Long rcId = category.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Cannot modify default category name
    if (category.getIsDefault() && name != null && !category.getName().equals(name)) {
      throw new IllegalArgumentException("Cannot change the name of a default category");
    }

    // Check if new name conflicts with existing
    if (name != null && !category.getName().equals(name) &&
        categoryRepository.existsByNameAndFiscalYear(name, category.getFiscalYear())) {
      throw new IllegalArgumentException("A category with this name already exists for this Fiscal Year");
    }

    if (name != null && !category.getIsDefault()) {
      category.setName(name);
    }
    if (description != null) {
      category.setDescription(description);
    }

    SpendingCategory saved = categoryRepository.save(category);
    logger.info("Updated spending category '" + category.getName() + "' by user " + username);

    return SpendingCategoryDTO.fromEntity(saved);
  }

  @Override
  public void deleteCategory(Long categoryId, String username) {
    Optional<SpendingCategory> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isEmpty()) {
      throw new IllegalArgumentException("Category not found");
    }

    SpendingCategory category = categoryOpt.get();
    Long rcId = category.getFiscalYear().getResponsibilityCentre().getId();

    // Verify user has write access to the RC
    if (!hasWriteAccessToRC(rcId, username)) {
      throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
    }

    // Cannot delete default categories
    if (category.getIsDefault()) {
      throw new IllegalArgumentException("Cannot delete a default category");
    }

    categoryRepository.delete(category);
    logger.info("Deleted spending category '" + category.getName() + "' from fiscal year " +
        category.getFiscalYear().getName() + " by user " + username);
  }

  @Override
  public List<SpendingCategoryDTO> ensureDefaultCategoriesExist(Long fiscalYearId, String username) {
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
    List<SpendingCategory> existingDefaults = categoryRepository.findDefaultCategoriesByFiscalYearId(fiscalYearId);
    if (!existingDefaults.isEmpty()) {
      // Return all categories
      return getCategoriesByFiscalYearId(fiscalYearId, username);
    }

    // Create default categories
    for (int i = 0; i < DEFAULT_CATEGORIES.length; i++) {
      String categoryName = DEFAULT_CATEGORIES[i][0];
      String categoryDescription = DEFAULT_CATEGORIES[i][1];

      // Check if category with this name already exists
      if (!categoryRepository.existsByNameAndFiscalYear(categoryName, fy)) {
        SpendingCategory category = new SpendingCategory(categoryName, categoryDescription, fy, true);
        category.setDisplayOrder(i);
        categoryRepository.save(category);
        logger.info("Created default spending category '" + categoryName + "' for fiscal year " + fy.getName());
      }
    }

    return getCategoriesByFiscalYearId(fiscalYearId, username);
  }

  @Override
  public List<SpendingCategoryDTO> reorderCategories(Long fiscalYearId, String username, List<Long> categoryIds) {
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
      Optional<SpendingCategory> categoryOpt = categoryRepository.findById(categoryId);
      if (categoryOpt.isPresent()) {
        SpendingCategory category = categoryOpt.get();
        if (category.getFiscalYear().getId().equals(fiscalYearId)) {
          category.setDisplayOrder(i);
          categoryRepository.save(category);
        }
      }
    }

    logger.info("Reordered spending categories for fiscal year " + fy.getName() + " by user " + username);
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
    List<SpendingCategory> existingDefaults = categoryRepository.findDefaultCategoriesByFiscalYearId(fiscalYearId);
    if (!existingDefaults.isEmpty()) {
      return;
    }

    // Create default categories
    for (int i = 0; i < DEFAULT_CATEGORIES.length; i++) {
      String categoryName = DEFAULT_CATEGORIES[i][0];
      String categoryDescription = DEFAULT_CATEGORIES[i][1];

      // Check if category with this name already exists
      if (!categoryRepository.existsByNameAndFiscalYear(categoryName, fy)) {
        SpendingCategory category = new SpendingCategory(categoryName, categoryDescription, fy, true);
        category.setDisplayOrder(i);
        categoryRepository.save(category);
        logger.info("Created default spending category '" + categoryName + "' for fiscal year " + fy.getName());
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
