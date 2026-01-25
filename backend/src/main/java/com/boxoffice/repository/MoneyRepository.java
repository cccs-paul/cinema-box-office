/*
 * myRC - Money Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * Repository interface for Money entity operations.
 */
package com.boxoffice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.Money;

/**
 * Repository interface for Money entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@Repository
public interface MoneyRepository extends JpaRepository<Money, Long> {

  /**
   * Find all monies for a specific fiscal year.
   *
   * @param fiscalYear the fiscal year
   * @return list of monies ordered by display order then code
   */
  List<Money> findByFiscalYearOrderByDisplayOrderAscCodeAsc(FiscalYear fiscalYear);

  /**
   * Find all monies for a specific fiscal year by ID.
   *
   * @param fiscalYearId the fiscal year ID
   * @return list of monies ordered by display order then code
   */
  @Query("SELECT m FROM Money m WHERE m.fiscalYear.id = :fyId ORDER BY m.displayOrder ASC, m.code ASC")
  List<Money> findByFiscalYearId(@Param("fyId") Long fiscalYearId);

  /**
   * Find all active monies for a fiscal year.
   *
   * @param fiscalYear the fiscal year
   * @param active whether the money is active
   * @return list of active monies
   */
  List<Money> findByFiscalYearAndActiveOrderByDisplayOrderAscCodeAsc(FiscalYear fiscalYear, Boolean active);

  /**
   * Check if a money with the same code exists for a fiscal year.
   *
   * @param code the money code
   * @param fiscalYear the fiscal year
   * @return true if exists
   */
  boolean existsByCodeAndFiscalYear(String code, FiscalYear fiscalYear);

  /**
   * Find a money by code and fiscal year.
   *
   * @param code the money code
   * @param fiscalYear the fiscal year
   * @return optional money
   */
  Optional<Money> findByCodeAndFiscalYear(String code, FiscalYear fiscalYear);

  /**
   * Find all monies for a responsibility centre (across all fiscal years).
   *
   * @param rcId the responsibility centre ID
   * @return list of monies
   */
  @Query("SELECT m FROM Money m WHERE m.fiscalYear.responsibilityCentre.id = :rcId ORDER BY m.fiscalYear.name ASC, m.displayOrder ASC, m.code ASC")
  List<Money> findByResponsibilityCentreId(@Param("rcId") Long rcId);

  /**
   * Find default money (AB) for a fiscal year.
   *
   * @param fiscalYear the fiscal year
   * @return optional default money
   */
  Optional<Money> findByFiscalYearAndIsDefaultTrue(FiscalYear fiscalYear);

  /**
   * Get the maximum display order for monies in a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return the maximum display order, or null if no monies exist
   */
  @Query("SELECT MAX(m.displayOrder) FROM Money m WHERE m.fiscalYear.id = :fyId")
  Integer findMaxDisplayOrderByFiscalYearId(@Param("fyId") Long fiscalYearId);

  /**
   * Count non-default monies for a fiscal year.
   *
   * @param fiscalYearId the fiscal year ID
   * @return count of custom (non-default) monies
   */
  @Query("SELECT COUNT(m) FROM Money m WHERE m.fiscalYear.id = :fyId AND m.isDefault = false")
  long countCustomMoniesByFiscalYearId(@Param("fyId") Long fiscalYearId);
}
