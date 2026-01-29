/*
 * myRC - Spending Item Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for SpendingItem entity operations.
 */
package com.boxoffice.repository;

import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.SpendingCategory;
import com.boxoffice.model.SpendingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for SpendingItem entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Repository
public interface SpendingItemRepository extends JpaRepository<SpendingItem, Long> {

  /**
   * Find all spending items for a fiscal year ordered by category then name.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of spending items
   */
  @Query("SELECT s FROM SpendingItem s LEFT JOIN FETCH s.category WHERE s.fiscalYear.id = :fiscalYearId " +
         "ORDER BY s.category.name ASC, s.name ASC")
  List<SpendingItem> findByFiscalYearIdOrderByCategoryNameAscNameAsc(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Find all spending items for a fiscal year ordered by name.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of spending items
   */
  List<SpendingItem> findByFiscalYearIdOrderByNameAsc(Long fiscalYearId);

  /**
   * Find all spending items for a category.
   *
   * @param categoryId the category ID
   * @return list of spending items
   */
  List<SpendingItem> findByCategoryIdOrderByNameAsc(Long categoryId);

  /**
   * Find all spending items for a fiscal year and category.
   *
   * @param fiscalYearId the fiscal year ID
   * @param categoryId the category ID
   * @return list of spending items
   */
  @Query("SELECT s FROM SpendingItem s LEFT JOIN FETCH s.category WHERE s.fiscalYear.id = :fiscalYearId " +
         "AND s.category.id = :categoryId ORDER BY s.name ASC")
  List<SpendingItem> findByFiscalYearIdAndCategoryId(@Param("fiscalYearId") Long fiscalYearId, 
                                                       @Param("categoryId") Long categoryId);

  /**
   * Find a spending item by name and fiscal year.
   *
   * @param name the item name
   * @param fiscalYear the fiscal year
   * @return optional spending item
   */
  Optional<SpendingItem> findByNameAndFiscalYear(String name, FiscalYear fiscalYear);

  /**
   * Check if a spending item exists by name and fiscal year.
   *
   * @param name the item name
   * @param fiscalYear the fiscal year
   * @return true if exists
   */
  boolean existsByNameAndFiscalYear(String name, FiscalYear fiscalYear);

  /**
   * Count spending items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return count of spending items
   */
  @Query("SELECT COUNT(s) FROM SpendingItem s WHERE s.fiscalYear.id = :fiscalYearId")
  long countByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Count spending items for a category.
   *
   * @param categoryId the category ID
   * @return count of spending items
   */
  @Query("SELECT COUNT(s) FROM SpendingItem s WHERE s.category.id = :categoryId")
  long countByCategoryId(@Param("categoryId") Long categoryId);

  /**
   * Get total amount for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return total amount or null if no items
   */
  @Query("SELECT SUM(s.amount) FROM SpendingItem s WHERE s.fiscalYear.id = :fiscalYearId")
  BigDecimal sumAmountByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Get total amount for a category.
   *
   * @param categoryId the category ID
   * @return total amount or null if no items
   */
  @Query("SELECT SUM(s.amount) FROM SpendingItem s WHERE s.category.id = :categoryId")
  BigDecimal sumAmountByCategoryId(@Param("categoryId") Long categoryId);

  /**
   * Find spending items by status.
   *
   * @param fiscalYearId the fiscal year ID
   * @param status the status
   * @return list of spending items
   */
  @Query("SELECT s FROM SpendingItem s WHERE s.fiscalYear.id = :fiscalYearId AND s.status = :status " +
         "ORDER BY s.name ASC")
  List<SpendingItem> findByFiscalYearIdAndStatus(@Param("fiscalYearId") Long fiscalYearId, 
                                                   @Param("status") SpendingItem.Status status);

  /**
   * Delete all spending items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   */
  void deleteByFiscalYearId(Long fiscalYearId);

  /**
   * Delete all spending items for a category.
   *
   * @param categoryId the category ID
   */
  void deleteByCategoryId(Long categoryId);
}
