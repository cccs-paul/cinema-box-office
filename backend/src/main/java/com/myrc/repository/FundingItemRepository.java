/*
 * myRC - Funding Item Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * Spring Data JPA Repository for FundingItem entity.
 */
package com.myrc.repository;

import com.myrc.model.FiscalYear;
import com.myrc.model.FundingItem;
import com.myrc.model.FundingSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for FundingItem entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@Repository
public interface FundingItemRepository extends JpaRepository<FundingItem, Long> {

  /**
   * Find all funding items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of funding items
   */
  List<FundingItem> findByFiscalYearId(Long fiscalYearId);

  /**
   * Find all funding items for a fiscal year ordered by name.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of funding items ordered by name
   */
  @Query("SELECT fi FROM FundingItem fi WHERE fi.fiscalYear.id = :fiscalYearId ORDER BY fi.name ASC")
  List<FundingItem> findByFiscalYearIdOrderByNameAsc(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Find all active funding items for a fiscal year ordered by name.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of active funding items ordered by name
   */
  @Query("SELECT fi FROM FundingItem fi WHERE fi.fiscalYear.id = :fiscalYearId AND fi.active = true ORDER BY fi.name ASC")
  List<FundingItem> findActiveFundingItemsByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Find funding items by source within a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @param source the funding source to filter by
   * @return list of funding items with the specified source
   */
  @Query("SELECT fi FROM FundingItem fi WHERE fi.fiscalYear.id = :fiscalYearId AND fi.source = :source ORDER BY fi.name ASC")
  List<FundingItem> findByFiscalYearIdAndSource(@Param("fiscalYearId") Long fiscalYearId, @Param("source") FundingSource source);

  /**
   * Check if a funding item with the given name exists for a fiscal year.
   *
   * @param name the funding item name
   * @param fiscalYear the fiscal year
   * @return true if exists
   */
  boolean existsByNameAndFiscalYear(String name, FiscalYear fiscalYear);

  /**
   * Find a funding item by name and fiscal year.
   *
   * @param name the funding item name
   * @param fiscalYearId the fiscal year ID
   * @return optional funding item
   */
  @Query("SELECT fi FROM FundingItem fi WHERE fi.name = :name AND fi.fiscalYear.id = :fiscalYearId")
  Optional<FundingItem> findByNameAndFiscalYearId(@Param("name") String name, @Param("fiscalYearId") Long fiscalYearId);

  /**
   * Count funding items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return count of funding items
   */
  long countByFiscalYearId(Long fiscalYearId);

  /**
   * Count active funding items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return count of active funding items
   */
  @Query("SELECT COUNT(fi) FROM FundingItem fi WHERE fi.fiscalYear.id = :fiscalYearId AND fi.active = true")
  long countActiveFundingItemsByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  /**
   * Delete all funding items for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   */
  @Modifying
  @Query("DELETE FROM FundingItem fi WHERE fi.fiscalYear.id = :fiscalYearId")
  void deleteByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);
}
