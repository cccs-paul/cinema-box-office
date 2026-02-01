/*
 * myRC - Procurement Item Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for ProcurementItem entity operations.
 */
package com.myrc.repository;

import com.myrc.model.FiscalYear;
import com.myrc.model.ProcurementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ProcurementItem entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
@Repository
public interface ProcurementItemRepository extends JpaRepository<ProcurementItem, Long> {

    /**
     * Find all procurement items for a fiscal year ordered by PR number.
     *
     * @param fiscalYearId the fiscal year ID
     * @return list of procurement items
     */
    List<ProcurementItem> findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(Long fiscalYearId);

    /**
     * Find all procurement items for a fiscal year ordered by name.
     *
     * @param fiscalYearId the fiscal year ID
     * @return list of procurement items
     */
    List<ProcurementItem> findByFiscalYearIdAndActiveTrueOrderByNameAsc(Long fiscalYearId);

    /**
     * Find procurement items by status for a fiscal year.
     *
     * @param fiscalYearId the fiscal year ID
     * @param status the status to filter by
     * @return list of procurement items
     */
    List<ProcurementItem> findByFiscalYearIdAndStatusAndActiveTrueOrderByPurchaseRequisitionAsc(
            Long fiscalYearId, ProcurementItem.Status status);

    /**
     * Find a procurement item by PR and fiscal year.
     *
     * @param purchaseRequisition the PR number
     * @param fiscalYear the fiscal year
     * @return optional procurement item
     */
    Optional<ProcurementItem> findByPurchaseRequisitionAndFiscalYearAndActiveTrue(
            String purchaseRequisition, FiscalYear fiscalYear);

    /**
     * Find a procurement item by PO number and fiscal year.
     *
     * @param purchaseOrder the PO number
     * @param fiscalYear the fiscal year
     * @return optional procurement item
     */
    Optional<ProcurementItem> findByPurchaseOrderAndFiscalYearAndActiveTrue(
            String purchaseOrder, FiscalYear fiscalYear);

    /**
     * Check if a procurement item exists by PR and fiscal year.
     *
     * @param purchaseRequisition the PR number
     * @param fiscalYear the fiscal year
     * @return true if exists
     */
    boolean existsByPurchaseRequisitionAndFiscalYearAndActiveTrue(String purchaseRequisition, FiscalYear fiscalYear);

    /**
     * Check if a procurement item exists by PO and fiscal year.
     *
     * @param purchaseOrder the PO number
     * @param fiscalYear the fiscal year
     * @return true if exists
     */
    boolean existsByPurchaseOrderAndFiscalYearAndActiveTrue(String purchaseOrder, FiscalYear fiscalYear);

    /**
     * Count procurement items for a fiscal year.
     *
     * @param fiscalYearId the fiscal year ID
     * @return count of procurement items
     */
    @Query("SELECT COUNT(p) FROM ProcurementItem p WHERE p.fiscalYear.id = :fiscalYearId AND p.active = true")
    long countByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);

    /**
     * Count procurement items by status for a fiscal year.
     *
     * @param fiscalYearId the fiscal year ID
     * @param status the status to count
     * @return count of procurement items
     */
    @Query("SELECT COUNT(p) FROM ProcurementItem p WHERE p.fiscalYear.id = :fiscalYearId AND p.status = :status AND p.active = true")
    long countByFiscalYearIdAndStatus(@Param("fiscalYearId") Long fiscalYearId, @Param("status") ProcurementItem.Status status);

    /**
     * Search procurement items by name or PR/PO containing the search term.
     *
     * @param fiscalYearId the fiscal year ID
     * @param searchTerm the search term
     * @return list of matching procurement items
     */
    @Query("SELECT p FROM ProcurementItem p WHERE p.fiscalYear.id = :fiscalYearId AND p.active = true " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.purchaseRequisition) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.purchaseOrder) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY p.purchaseRequisition ASC")
    List<ProcurementItem> searchByNameOrPrOrPo(@Param("fiscalYearId") Long fiscalYearId, @Param("searchTerm") String searchTerm);

    /**
     * Find procurement item by ID with quotes eagerly loaded.
     *
     * @param id the procurement item ID
     * @return optional procurement item with quotes
     */
    @Query("SELECT p FROM ProcurementItem p LEFT JOIN FETCH p.quotes WHERE p.id = :id AND p.active = true")
    Optional<ProcurementItem> findByIdWithQuotes(@Param("id") Long id);

    /**
     * Delete all procurement items for a fiscal year.
     *
     * @param fiscalYearId the fiscal year ID
     */
    @Modifying
    @Query("DELETE FROM ProcurementItem pi WHERE pi.fiscalYear.id = :fiscalYearId")
    void deleteByFiscalYearId(@Param("fiscalYearId") Long fiscalYearId);
}
