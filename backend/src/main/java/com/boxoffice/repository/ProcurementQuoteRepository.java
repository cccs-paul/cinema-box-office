/*
 * myRC - Procurement Quote Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for ProcurementQuote entity operations.
 */
package com.boxoffice.repository;

import com.boxoffice.model.ProcurementQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ProcurementQuote entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
@Repository
public interface ProcurementQuoteRepository extends JpaRepository<ProcurementQuote, Long> {

    /**
     * Find all quotes for a procurement item ordered by vendor name.
     *
     * @param procurementItemId the procurement item ID
     * @return list of quotes
     */
    List<ProcurementQuote> findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(Long procurementItemId);

    /**
     * Find all quotes for a procurement item ordered by amount ascending.
     *
     * @param procurementItemId the procurement item ID
     * @return list of quotes
     */
    List<ProcurementQuote> findByProcurementItemIdAndActiveTrueOrderByAmountAsc(Long procurementItemId);

    /**
     * Find quotes by status for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @param status the status to filter by
     * @return list of quotes
     */
    List<ProcurementQuote> findByProcurementItemIdAndStatusAndActiveTrueOrderByVendorNameAsc(
            Long procurementItemId, ProcurementQuote.Status status);

    /**
     * Find the selected quote for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @return optional selected quote
     */
    Optional<ProcurementQuote> findByProcurementItemIdAndSelectedTrueAndActiveTrue(Long procurementItemId);

    /**
     * Count quotes for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @return count of quotes
     */
    @Query("SELECT COUNT(q) FROM ProcurementQuote q WHERE q.procurementItem.id = :procurementItemId AND q.active = true")
    long countByProcurementItemId(@Param("procurementItemId") Long procurementItemId);

    /**
     * Find quote by ID with files eagerly loaded.
     *
     * @param id the quote ID
     * @return optional quote with files
     */
    @Query("SELECT q FROM ProcurementQuote q LEFT JOIN FETCH q.files WHERE q.id = :id AND q.active = true")
    Optional<ProcurementQuote> findByIdWithFiles(@Param("id") Long id);

    /**
     * Find all quotes for a procurement item with files eagerly loaded.
     *
     * @param procurementItemId the procurement item ID
     * @return list of quotes with files
     */
    @Query("SELECT DISTINCT q FROM ProcurementQuote q LEFT JOIN FETCH q.files " +
           "WHERE q.procurementItem.id = :procurementItemId AND q.active = true ORDER BY q.vendorName ASC")
    List<ProcurementQuote> findByProcurementItemIdWithFiles(@Param("procurementItemId") Long procurementItemId);

    /**
     * Check if a quote exists by vendor name and procurement item.
     *
     * @param vendorName the vendor name
     * @param procurementItemId the procurement item ID
     * @return true if exists
     */
    boolean existsByVendorNameAndProcurementItemIdAndActiveTrue(String vendorName, Long procurementItemId);

    /**
     * Find the lowest amount quote for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     * @return optional quote with lowest amount
     */
    @Query("SELECT q FROM ProcurementQuote q WHERE q.procurementItem.id = :procurementItemId " +
           "AND q.active = true AND q.amount IS NOT NULL ORDER BY q.amount ASC LIMIT 1")
    Optional<ProcurementQuote> findLowestAmountQuote(@Param("procurementItemId") Long procurementItemId);

    /**
     * Delete all quotes for a procurement item.
     *
     * @param procurementItemId the procurement item ID
     */
    void deleteByProcurementItemId(Long procurementItemId);
}
