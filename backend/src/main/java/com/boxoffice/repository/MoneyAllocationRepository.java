/*
 * myRC - Money Allocation Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for MoneyAllocation entity operations.
 */
package com.boxoffice.repository;

import com.boxoffice.model.FundingItem;
import com.boxoffice.model.Money;
import com.boxoffice.model.MoneyAllocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for MoneyAllocation entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@Repository
public interface MoneyAllocationRepository extends JpaRepository<MoneyAllocation, Long> {

  /**
   * Find all allocations for a funding item.
   *
   * @param fundingItem the funding item
   * @return list of allocations
   */
  List<MoneyAllocation> findByFundingItem(FundingItem fundingItem);

  /**
   * Find all allocations for a funding item by ID.
   *
   * @param fundingItemId the funding item ID
   * @return list of allocations
   */
  List<MoneyAllocation> findByFundingItemId(Long fundingItemId);

  /**
   * Find allocation by funding item and money.
   *
   * @param fundingItem the funding item
   * @param money the money type
   * @return optional allocation
   */
  Optional<MoneyAllocation> findByFundingItemAndMoney(FundingItem fundingItem, Money money);

  /**
   * Find allocation by funding item ID and money ID.
   *
   * @param fundingItemId the funding item ID
   * @param moneyId the money ID
   * @return optional allocation
   */
  Optional<MoneyAllocation> findByFundingItemIdAndMoneyId(Long fundingItemId, Long moneyId);

  /**
   * Check if an allocation exists for a funding item and money combination.
   *
   * @param fundingItem the funding item
   * @param money the money type
   * @return true if allocation exists
   */
  boolean existsByFundingItemAndMoney(FundingItem fundingItem, Money money);

  /**
   * Delete all allocations for a funding item.
   *
   * @param fundingItem the funding item
   */
  @Modifying
  @Query("DELETE FROM MoneyAllocation ma WHERE ma.fundingItem = :fundingItem")
  void deleteByFundingItem(@Param("fundingItem") FundingItem fundingItem);

  /**
   * Delete all allocations for a funding item by ID.
   *
   * @param fundingItemId the funding item ID
   */
  @Modifying
  @Query("DELETE FROM MoneyAllocation ma WHERE ma.fundingItem.id = :fundingItemId")
  void deleteByFundingItemId(@Param("fundingItemId") Long fundingItemId);

  /**
   * Find all allocations for a specific money type.
   *
   * @param money the money type
   * @return list of allocations
   */
  List<MoneyAllocation> findByMoney(Money money);

  /**
   * Delete all allocations for a money type.
   *
   * @param money the money type
   */
  @Modifying
  @Query("DELETE FROM MoneyAllocation ma WHERE ma.money = :money")
  void deleteByMoney(@Param("money") Money money);

  /**
   * Get total CAP amount for a funding item.
   *
   * @param fundingItemId the funding item ID
   * @return total CAP amount or null if no allocations
   */
  @Query("SELECT SUM(ma.capAmount) FROM MoneyAllocation ma WHERE ma.fundingItem.id = :fundingItemId")
  java.math.BigDecimal sumCapAmountByFundingItemId(@Param("fundingItemId") Long fundingItemId);

  /**
   * Get total OM amount for a funding item.
   *
   * @param fundingItemId the funding item ID
   * @return total OM amount or null if no allocations
   */
  @Query("SELECT SUM(ma.omAmount) FROM MoneyAllocation ma WHERE ma.fundingItem.id = :fundingItemId")
  java.math.BigDecimal sumOmAmountByFundingItemId(@Param("fundingItemId") Long fundingItemId);
}
