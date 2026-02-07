/*
 * myRC - Spending Money Allocation Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for SpendingMoneyAllocation entity operations.
 */
package com.myrc.repository;

import com.myrc.model.SpendingMoneyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for SpendingMoneyAllocation entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Repository
public interface SpendingMoneyAllocationRepository extends JpaRepository<SpendingMoneyAllocation, Long> {

  /**
   * Find all allocations for a spending item.
   *
   * @param spendingItemId the spending item ID
   * @return list of allocations
   */
  List<SpendingMoneyAllocation> findBySpendingItemId(Long spendingItemId);

  /**
   * Find allocation by spending item and money.
   *
   * @param spendingItemId the spending item ID
   * @param moneyId the money ID
   * @return optional allocation
   */
  @Query("SELECT a FROM SpendingMoneyAllocation a WHERE a.spendingItem.id = :spendingItemId AND a.money.id = :moneyId")
  Optional<SpendingMoneyAllocation> findBySpendingItemIdAndMoneyId(@Param("spendingItemId") Long spendingItemId, 
                                                                     @Param("moneyId") Long moneyId);

  /**
   * Delete all allocations for a spending item.
   *
   * @param spendingItemId the spending item ID
   */
  @Modifying
  @Query("DELETE FROM SpendingMoneyAllocation a WHERE a.spendingItem.id = :spendingItemId")
  void deleteBySpendingItemId(@Param("spendingItemId") Long spendingItemId);

  /**
   * Get total CAP amount for a spending item.
   *
   * @param spendingItemId the spending item ID
   * @return total CAP amount or null if no allocations
   */
  @Query("SELECT SUM(a.capAmount) FROM SpendingMoneyAllocation a WHERE a.spendingItem.id = :spendingItemId")
  BigDecimal sumCapAmountBySpendingItemId(@Param("spendingItemId") Long spendingItemId);

  /**
   * Get total OM amount for a spending item.
   *
   * @param spendingItemId the spending item ID
   * @return total OM amount or null if no allocations
   */
  @Query("SELECT SUM(a.omAmount) FROM SpendingMoneyAllocation a WHERE a.spendingItem.id = :spendingItemId")
  BigDecimal sumOmAmountBySpendingItemId(@Param("spendingItemId") Long spendingItemId);

  /**
   * Check if a money type has any spending allocations with non-zero CAP or OM amounts.
   *
   * @param moneyId the money ID
   * @return true if any allocation has non-zero amounts
   */
  @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM SpendingMoneyAllocation a " +
         "WHERE a.money.id = :moneyId AND (a.capAmount <> 0 OR a.omAmount <> 0)")
  boolean hasNonZeroAllocationsByMoneyId(@Param("moneyId") Long moneyId);

  /**
   * Delete all spending allocations for a money type.
   *
   * @param moneyId the money ID
   */
  @Modifying
  @Query("DELETE FROM SpendingMoneyAllocation a WHERE a.money.id = :moneyId")
  void deleteByMoneyId(@Param("moneyId") Long moneyId);

  /**
   * Get total CAP amount for a money type across all spending items in a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param moneyId the money ID
   * @return total CAP amount
   */
  @Query("SELECT COALESCE(SUM(a.capAmount), 0) FROM SpendingMoneyAllocation a " +
         "WHERE a.spendingItem.fiscalYear.id = :fiscalYearId AND a.money.id = :moneyId")
  BigDecimal sumCapAmountByFiscalYearIdAndMoneyId(@Param("fiscalYearId") Long fiscalYearId, 
                                                    @Param("moneyId") Long moneyId);

  /**
   * Get total OM amount for a money type across all spending items in a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param moneyId the money ID
   * @return total OM amount
   */
  @Query("SELECT COALESCE(SUM(a.omAmount), 0) FROM SpendingMoneyAllocation a " +
         "WHERE a.spendingItem.fiscalYear.id = :fiscalYearId AND a.money.id = :moneyId")
  BigDecimal sumOmAmountByFiscalYearIdAndMoneyId(@Param("fiscalYearId") Long fiscalYearId, 
                                                   @Param("moneyId") Long moneyId);
}
