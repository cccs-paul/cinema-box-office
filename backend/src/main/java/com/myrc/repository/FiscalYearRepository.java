/*
 * myRC - Fiscal Year Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrc.model.FiscalYear;
import com.myrc.model.ResponsibilityCentre;

/**
 * Repository interface for FiscalYear entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-22
 */
@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {

  /**
   * Find all fiscal years for a specific responsibility centre.
   *
   * @param responsibilityCentre the responsibility centre
   * @return list of fiscal years
   */
  List<FiscalYear> findByResponsibilityCentreOrderByNameAsc(ResponsibilityCentre responsibilityCentre);

  /**
   * Find all fiscal years for a specific responsibility centre by ID.
   *
   * @param rcId the responsibility centre ID
   * @return list of fiscal years
   */
  @Query("SELECT fy FROM FiscalYear fy WHERE fy.responsibilityCentre.id = :rcId ORDER BY fy.name ASC")
  List<FiscalYear> findByResponsibilityCentreId(@Param("rcId") Long rcId);

  /**
   * Find active fiscal years for a responsibility centre.
   *
   * @param responsibilityCentre the responsibility centre
   * @return list of active fiscal years
   */
  List<FiscalYear> findByResponsibilityCentreAndActiveOrderByNameAsc(ResponsibilityCentre responsibilityCentre, Boolean active);

  /**
   * Check if a fiscal year with the same name exists for a responsibility centre.
   *
   * @param name the fiscal year name
   * @param responsibilityCentre the responsibility centre
   * @return true if exists
   */
  boolean existsByNameAndResponsibilityCentre(String name, ResponsibilityCentre responsibilityCentre);

  /**
   * Find a fiscal year by name and responsibility centre.
   *
   * @param name the fiscal year name
   * @param responsibilityCentre the responsibility centre
   * @return optional fiscal year
   */
  Optional<FiscalYear> findByNameAndResponsibilityCentre(String name, ResponsibilityCentre responsibilityCentre);

  /**
   * Delete all fiscal years for a specific responsibility centre.
   *
   * @param responsibilityCentre the responsibility centre
   */
  @Modifying
  @Query("DELETE FROM FiscalYear fy WHERE fy.responsibilityCentre = :rc")
  void deleteByResponsibilityCentre(@Param("rc") ResponsibilityCentre responsibilityCentre);

  /**
   * Delete all fiscal years for a specific responsibility centre by ID.
   *
   * @param rcId the responsibility centre ID
   */
  @Modifying
  @Query("DELETE FROM FiscalYear fy WHERE fy.responsibilityCentre.id = :rcId")
  void deleteByResponsibilityCentreId(@Param("rcId") Long rcId);
}
