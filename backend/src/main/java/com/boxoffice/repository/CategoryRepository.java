/*
 * myRC - Category Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-27
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for Category entity operations.
 */
package com.boxoffice.repository;

import com.boxoffice.model.Category;
import com.boxoffice.model.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Category entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /**
   * Find all categories for a fiscal year ordered by display order then name.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of categories
   */
  @Query("SELECT c FROM Category c WHERE c.fiscalYear.id = :fiscalYearId " +
         "ORDER BY c.displayOrder ASC, c.name ASC")
  List<Category> findByFiscalYearIdOrderByDisplayOrderAscNameAsc(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Find all active categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of active categories
   */
  @Query("SELECT c FROM Category c WHERE c.fiscalYear.id = :fiscalYearId AND c.active = true " +
         "ORDER BY c.displayOrder ASC, c.name ASC")
  List<Category> findActiveCategoriesByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Find a category by name and fiscal year.
   *
   * @param name the category name
   * @param fiscalYear the fiscal year
   * @return optional category
   */
  Optional<Category> findByNameAndFiscalYear(String name, FiscalYear fiscalYear);

  /**
   * Check if a category exists by name and fiscal year.
   *
   * @param name the category name
   * @param fiscalYear the fiscal year
   * @return true if exists
   */
  boolean existsByNameAndFiscalYear(String name, FiscalYear fiscalYear);

  /**
   * Find all default categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of default categories
   */
  @Query("SELECT c FROM Category c WHERE c.fiscalYear.id = :fiscalYearId AND c.isDefault = true " +
         "ORDER BY c.displayOrder ASC")
  List<Category> findDefaultCategoriesByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Find all custom (non-default) categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of custom categories
   */
  @Query("SELECT c FROM Category c WHERE c.fiscalYear.id = :fiscalYearId AND c.isDefault = false " +
         "ORDER BY c.displayOrder ASC, c.name ASC")
  List<Category> findCustomCategoriesByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Count categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return count of categories
   */
  @Query("SELECT COUNT(c) FROM Category c WHERE c.fiscalYear.id = :fiscalYearId")
  long countByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Get max display order for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return max display order or 0 if none
   */
  @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM Category c WHERE c.fiscalYear.id = :fiscalYearId")
  int getMaxDisplayOrderByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Delete all categories for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   */
  void deleteByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);
}
