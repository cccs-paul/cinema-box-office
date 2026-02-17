/*
 * myRC - Travel Item Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.TravelItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for TravelItem entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Repository
public interface TravelItemRepository extends JpaRepository<TravelItem, Long> {

  List<TravelItem> findByFiscalYearIdOrderByNameAsc(Long fiscalYearId);

  @Query("SELECT t FROM TravelItem t WHERE t.fiscalYear.id = :fiscalYearId AND t.active = true ORDER BY t.name ASC")
  List<TravelItem> findActiveByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  Optional<TravelItem> findByNameAndFiscalYearId(String name, Long fiscalYearId);

  boolean existsByNameAndFiscalYearId(String name, Long fiscalYearId);

  @Query("SELECT COUNT(t) FROM TravelItem t WHERE t.fiscalYear.id = :fiscalYearId")
  long countByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

  @Query("SELECT COUNT(t) FROM TravelItem t WHERE t.fiscalYear.id = :fiscalYearId AND t.status = :status")
  long countByFiscalYearIdAndStatus(@Param("fiscalYearId") Long fiscalYearId,
                                     @Param("status") TravelItem.Status status);
}
